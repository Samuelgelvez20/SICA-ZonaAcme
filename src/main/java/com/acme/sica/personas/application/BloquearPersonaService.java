package com.acme.sica.personas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.shared.EntidadDuplicadaException;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;

public class BloquearPersonaService {

    private final AutorizarAccionService autorizarAccionService;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;

    public BloquearPersonaService(AutorizarAccionService autorizarAccionService,
                                   PersonaRepository personaRepository,
                                   AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.personaRepository = personaRepository;
        this.auditoriaService = auditoriaService;
    }

    public Persona ejecutar(Usuario usuarioActual, Long personaId, String motivo) {
        // a) Autorizacion
        autorizarAccionService.verificar(usuarioActual, "bloquear_persona");

        // b) Validar personaId
        if (personaId == null) {
            auditoriaService.registrar(usuarioActual.getId(), "BLOQUEAR_PERSONA", "personas",
                    "Intento de bloquear persona: personaId es null",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("El ID de la persona es obligatorio");
        }

        // c) Buscar persona
        var personaOpt = personaRepository.buscarPorId(personaId);
        if (personaOpt.isEmpty()) {
            auditoriaService.registrar(usuarioActual.getId(), "BLOQUEAR_PERSONA", "personas",
                    "Intento de bloquear persona: id=" + personaId + " no encontrada",
                    ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porId(personaId);
        }
        Persona persona = personaOpt.get();

        // d) Verificar si ya esta bloqueada
        if (persona.isBloqueado()) {
            auditoriaService.registrar(usuarioActual.getId(), "BLOQUEAR_PERSONA", "personas",
                    "Intento de bloquear persona: id=" + personaId
                    + ", documento=" + persona.getDocumento() + " ya esta bloqueada",
                    ResultadoAuditoria.FALLO);
            throw new EntidadDuplicadaException(
                    "La persona con id " + personaId + " ya esta bloqueada");
        }

        // e) Validar motivo
        if (motivo == null || motivo.isBlank()) {
            auditoriaService.registrar(usuarioActual.getId(), "BLOQUEAR_PERSONA", "personas",
                    "Intento de bloquear persona: id=" + personaId + ", motivo vacio",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("El motivo del bloqueo es obligatorio");
        }

        // f) Bloquear
        persona.setBloqueado(true);
        persona.setMotivoBloqueo(motivo);

        // g) Persistir
        Persona actualizada = personaRepository.actualizar(persona);

        // h) Auditar exito
        String detalle = "personaId=" + actualizada.getId()
                + ", documento=" + actualizada.getDocumento()
                + ", nombre=" + actualizada.getNombre()
                + ", motivo=" + motivo;
        auditoriaService.registrar(usuarioActual.getId(), "BLOQUEAR_PERSONA", "personas",
                detalle, ResultadoAuditoria.EXITO);

        // i) Retornar
        return actualizada;
    }
}
