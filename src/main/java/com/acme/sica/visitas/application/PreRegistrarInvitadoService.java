package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.visitas.domain.VisitaFactory;

import java.time.LocalDateTime;

public class PreRegistrarInvitadoService {

    private final AutorizarAccionService autorizarAccionService;
    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;

    public PreRegistrarInvitadoService(AutorizarAccionService autorizarAccionService,
                                       PersonaRepository personaRepository,
                                       VisitaRepository visitaRepository,
                                       AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
    }

    public Visita ejecutar(Usuario usuarioActual, Long personaId, Long empresaVisitadaId,
                           LocalDateTime fechaHoraProgramada) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "registrar_visita");

        // b) Verificar que la persona exista
        if (!personaRepository.buscarPorId(personaId).isPresent()) {
            auditoriaService.registrar(usuarioActual.getId(), "PRE_REGISTRAR_VISITA", "visitas",
                    "Intento de pre-registro: personaId=" + personaId + " no existe",
                    ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porId(personaId);
        }

        // c) Crear la visita usando Factory Method
        Visita visita = VisitaFactory.crearPreRegistrada(
                personaId, usuarioActual.getId(), empresaVisitadaId, fechaHoraProgramada
        );

        // d) Guardar
        Visita guardada = visitaRepository.guardar(visita);

        // e) Auditar éxito
        auditoriaService.registrar(usuarioActual.getId(), "PRE_REGISTRAR_VISITA", "visitas",
                "personaId=" + personaId + ", empresaVisitadaId=" + empresaVisitadaId +
                ", fechaHoraProgramada=" + fechaHoraProgramada,
                ResultadoAuditoria.EXITO);

        return guardada;
    }
}