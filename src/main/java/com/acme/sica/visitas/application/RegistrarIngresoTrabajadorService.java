package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.shared.PersonaBloqueadaException;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.shared.ValidacionIngresoException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.visitas.domain.VisitaFactory;

import java.time.LocalDateTime;

public class RegistrarIngresoTrabajadorService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;

    public RegistrarIngresoTrabajadorService(PersonaRepository personaRepository,
                                              AutorizarAccionService autorizarAccionService,
                                              VisitaRepository visitaRepository,
                                              AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
    }

    public Visita registrar(Usuario usuarioActual, String documento) {
        // a) Autorizar
        autorizarAccionService.verificar(usuarioActual, "checkin_visita");

        // b) Buscar persona por documento
        var personaOpt = personaRepository.buscarPorDocumento(documento);
        if (personaOpt.isEmpty()) {
            auditoriaService.registrar(usuarioActual.getId(), "INGRESO_DIRECTO_TRABAJADOR", "visitas",
                    "Intento de ingreso directo: documento '" + documento + "' no encontrado",
                    ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porDocumento(documento);
        }
        var persona = personaOpt.get();

        // c) Verificar tipo: solo TRABAJADOR
        if (persona.getTipo() != TipoPersona.TRABAJADOR) {
            auditoriaService.registrar(usuarioActual.getId(), "INGRESO_DIRECTO_TRABAJADOR", "visitas",
                    "Intento de ingreso directo con persona tipo " + persona.getTipo()
                    + ", documento='" + documento + "', nombre='" + persona.getNombre() + "'",
                    ResultadoAuditoria.FALLO);
            throw new ValidacionIngresoException(
                    "Los invitados deben ingresar mediante pre-registro o el flujo de "
                    + "invitado no anunciado, no por esta vía.");
        }

        // d) Verificar bloqueo (reutiliza exactamente la lógica de RegistrarCheckInService)
        if (persona.isBloqueado()) {
            auditoriaService.registrar(usuarioActual.getId(), "INGRESO_DIRECTO_PERSONA_BLOQUEADA", "personas",
                    "personaId=" + persona.getId()
                    + ", documento=" + documento
                    + ", nombre=" + persona.getNombre()
                    + ", motivoBloqueo=" + persona.getMotivoBloqueo(),
                    ResultadoAuditoria.FALLO);
            throw PersonaBloqueadaException.conMotivo(documento, persona.getMotivoBloqueo());
        }

        // e) Regularización automática: cerrar visita DENTRO abierta (salida olvidada)
        var visitaActivaOpt = visitaRepository.buscarVisitaActivaPorPersona(persona.getId());
        if (visitaActivaOpt.isPresent()) {
            Visita visitaOlvidada = visitaActivaOpt.get();
            visitaOlvidada.setEstado(com.acme.sica.visitas.domain.EstadoVisita.CERRADA_POR_SISTEMA_SALIDA_OLVIDADA);
            visitaOlvidada.setFechaHoraSalida(LocalDateTime.now());
            visitaRepository.actualizar(visitaOlvidada);

            String detalleCierre = "Visita " + visitaOlvidada.getId() + " cerrada automaticamente por sistema: "
                    + "persona '" + documento + "' tenia una visita DENTRO sin cerrar";
            auditoriaService.registrar(usuarioActual.getId(), "CIERRE_AUTOMATICO_SALIDA_OLVIDADA", "visitas",
                    detalleCierre, ResultadoAuditoria.EXITO);
        }

        // f) Crear visita con ingreso directo
        Visita visita = VisitaFactory.crearIngresoDirecto(
                persona.getId(), usuarioActual.getId(), persona.getEmpresaId());

        // g) Guardar
        Visita guardada = visitaRepository.guardar(visita);

        // h) Auditar éxito
        String detalleExito = "personaId=" + persona.getId() + ", persona=" + persona.getNombre()
                + ", documento=" + documento + ", empresaId=" + persona.getEmpresaId();
        auditoriaService.registrar(usuarioActual.getId(), "INGRESO_DIRECTO_TRABAJADOR", "visitas",
                detalleExito, ResultadoAuditoria.EXITO);

        return guardada;
    }
}
