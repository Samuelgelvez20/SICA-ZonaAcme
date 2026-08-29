package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.shared.SinVisitaAprobadaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.EstadoVisita;
import com.acme.sica.visitas.domain.Visita;

import java.time.LocalDateTime;

public class RegistrarCheckInService {

    private final AutorizarAccionService autorizarAccionService;
    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;

    public RegistrarCheckInService(AutorizarAccionService autorizarAccionService,
                                   PersonaRepository personaRepository,
                                   VisitaRepository visitaRepository,
                                   AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
    }

    public Visita ejecutar(Usuario usuarioActual, String documento) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "checkin_visita");

        // b) Buscar persona por documento
        var personaOpt = personaRepository.buscarPorDocumento(documento);
        if (personaOpt.isEmpty()) {
            String detalle = "Intento de check-in: documento '" + documento + "' no encontrado";
            auditoriaService.registrar(usuarioActual.getId(), "CHECKIN_VISITA", "visitas",
                    detalle, ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porDocumento(documento);
        }
        var persona = personaOpt.get();

        // c) REGULARIZACIÓN AUTOMÁTICA: buscar visita DENTRO abierta (salida olvidada)
        var visitaActivaOpt = visitaRepository.buscarVisitaActivaPorPersona(persona.getId());
        if (visitaActivaOpt.isPresent()) {
            Visita visitaOlvidada = visitaActivaOpt.get();
            // Cerrar automáticamente la visita anterior
            visitaOlvidada.setEstado(EstadoVisita.CERRADA_POR_SISTEMA_SALIDA_OLVIDADA);
            visitaOlvidada.setFechaHoraSalida(LocalDateTime.now());
            visitaRepository.actualizar(visitaOlvidada);

            // Auditar cierre automático COMO EVENTO SEPARADO del check-in que sigue
            String detalleCierre = "Visita " + visitaOlvidada.getId() + " cerrada automaticamente por sistema: " +
                    "persona '" + documento + "' tenia una visita DENTRO sin cerrar";
            auditoriaService.registrar(usuarioActual.getId(), "CIERRE_AUTOMATICO_SALIDA_OLVIDADA", "visitas",
                    detalleCierre, ResultadoAuditoria.EXITO);
        }

        // d) Buscar visita aprobada pendiente de ingreso (flujo normal)
        var visitaOpt = visitaRepository.buscarVisitaAprobadaPendienteDeIngreso(persona.getId());
        if (visitaOpt.isEmpty()) {
            String detalle = "Intento de check-in: persona '" + documento + "' sin visita aprobada pendiente";
            auditoriaService.registrar(usuarioActual.getId(), "CHECKIN_VISITA", "visitas",
                    detalle, ResultadoAuditoria.FALLO);
            throw SinVisitaAprobadaException.porDocumento(documento);
        }

        // e) Actualizar la visita: estado = DENTRO, guardaId = usuarioActual, fechaHoraIngreso = now
        Visita visita = visitaOpt.get();
        visita.setEstado(EstadoVisita.DENTRO);
        visita.setGuardaId(usuarioActual.getId());
        visita.setFechaHoraIngreso(LocalDateTime.now());

        Visita actualizada = visitaRepository.actualizar(visita);

        // f) Auditar éxito del check-in
        String detalleExito = "personaId=" + persona.getId() + ", persona=" + persona.getNombre()
                + ", documento=" + documento + ", empresaVisitadaId=" + visita.getEmpresaVisitadaId();
        auditoriaService.registrar(usuarioActual.getId(), "CHECKIN_VISITA", "visitas",
                detalleExito, ResultadoAuditoria.EXITO);

        return actualizada;
    }
}