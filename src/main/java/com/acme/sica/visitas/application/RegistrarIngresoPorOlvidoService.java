package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.shared.ValidacionIngresoException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.ReglaValidacionIngreso;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.visitas.domain.VisitaFactory;

public class RegistrarIngresoPorOlvidoService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;
    private final NotificadorVisitas notificadorVisitas;
    private final ReglaValidacionIngreso reglaValidacionIngreso;

    public RegistrarIngresoPorOlvidoService(PersonaRepository personaRepository,
                                            AutorizarAccionService autorizarAccionService,
                                            VisitaRepository visitaRepository,
                                            AuditoriaService auditoriaService,
                                            NotificadorVisitas notificadorVisitas,
                                            ReglaValidacionIngreso reglaValidacionIngreso) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
        this.notificadorVisitas = notificadorVisitas;
        this.reglaValidacionIngreso = reglaValidacionIngreso;
    }

    public Visita registrar(Usuario usuarioActual, String documento) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "registrar_visita");

        // b) Buscar persona por documento (debe existir ya)
        Persona persona = personaRepository.buscarPorDocumento(documento)
                .orElseThrow(() -> PersonaNoEncontradaException.porDocumento(documento));

        // c) Aplicar validación Strategy
        try {
            reglaValidacionIngreso.validar(persona);
        } catch (ValidacionIngresoException e) {
            String detalle = "Intento de ingreso por olvido: persona='" + documento +
                    "' rechazada por validaci\u00f3n: " + e.getMessage();
            auditoriaService.registrar(usuarioActual.getId(), "INGRESO_POR_OLVIDO",
                    "visitas", detalle, ResultadoAuditoria.FALLO);
            throw e;
        }

        // d) Verificar funcionario anfitrión
        if (persona.getFuncionarioAnfitrionId() == null) {
            throw new IllegalStateException(
                    "La persona " + documento + " no tiene funcionario anfitri\u00f3n asignado; " +
                    "no se puede procesar ingreso por olvido sin saber a qui\u00e9n notificar");
        }

        // e) Crear visita vía Factory (funcionarioId y empresaVisitadaId vienen de la Persona)
        Visita visita = VisitaFactory.crearPorOlvido(persona.getId(), usuarioActual.getId(),
                persona.getFuncionarioAnfitrionId(), persona.getEmpresaId());

        // f) Guardar y auditar éxito
        Visita guardada = visitaRepository.guardar(visita);

        String detalleExito = "personaId=" + persona.getId() + ", persona=" + persona.getNombre() +
                ", documento=" + documento + ", funcionarioAnfitrionId=" + persona.getFuncionarioAnfitrionId() +
                ", empresaId=" + persona.getEmpresaId();
        auditoriaService.registrar(usuarioActual.getId(), "INGRESO_POR_OLVIDO",
                "visitas", detalleExito, ResultadoAuditoria.EXITO);

        // g) Notificar solicitud pendiente (asíncrono, DESPUÉS de guardar)
        notificadorVisitas.notificarSolicitudPendiente(guardada);

        return guardada;
    }
}