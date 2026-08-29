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

public class RegistrarCheckOutService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;

    public RegistrarCheckOutService(PersonaRepository personaRepository,
                                    AutorizarAccionService autorizarAccionService,
                                    VisitaRepository visitaRepository,
                                    AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
    }

    public Visita registrar(Usuario usuarioActual, String documento) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "checkout_visita");

        // b) Buscar persona por documento
        var personaOpt = personaRepository.buscarPorDocumento(documento);
        if (personaOpt.isEmpty()) {
            String detalle = "Intento de check-out: documento '" + documento + "' no encontrado";
            auditoriaService.registrar(usuarioActual.getId(), "CHECKOUT_VISITA", "visitas",
                    detalle, ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porDocumento(documento);
        }
        var persona = personaOpt.get();

        // c) Buscar visita activa (DENTRO) de esta persona
        var visitaOpt = visitaRepository.buscarVisitaActivaPorPersona(persona.getId());
        if (visitaOpt.isEmpty()) {
            String detalle = "Intento de check-out: persona '" + documento + "' sin visita activa (DENTRO) para cerrar";
            auditoriaService.registrar(usuarioActual.getId(), "CHECKOUT_VISITA", "visitas",
                    detalle, ResultadoAuditoria.FALLO);
            throw SinVisitaAprobadaException.porDocumento(documento);
        }

        // d) Actualizar: estado = CERRADA, fechaHoraSalida = now
        Visita visita = visitaOpt.get();
        visita.setEstado(EstadoVisita.CERRADA);
        visita.setFechaHoraSalida(LocalDateTime.now());
        Visita actualizada = visitaRepository.actualizar(visita);

        // e) Auditar éxito
        String detalleExito = "personaId=" + persona.getId() + ", persona=" + persona.getNombre() +
                ", documento=" + documento + ", visitaId=" + visita.getId();
        auditoriaService.registrar(usuarioActual.getId(), "CHECKOUT_VISITA", "visitas",
                detalleExito, ResultadoAuditoria.EXITO);

        return actualizada;
    }
}