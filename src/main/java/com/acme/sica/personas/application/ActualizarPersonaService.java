package com.acme.sica.personas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.personas.domain.Persona;

import java.util.NoSuchElementException;

public class ActualizarPersonaService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final AuditoriaService auditoriaService;

    public ActualizarPersonaService(PersonaRepository personaRepository,
                                     AutorizarAccionService autorizarAccionService,
                                     AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.auditoriaService = auditoriaService;
    }

    public Persona ejecutar(Usuario usuarioActual, Persona persona) {
        autorizarAccionService.verificar(usuarioActual, "editar_persona");

        if (persona.getId() == null) {
            auditoriaService.registrar(usuarioActual.getId(), "ACTUALIZAR_PERSONA", "personas",
                    "Intento de actualización con ID nulo",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("La persona debe tener un ID para actualizar");
        }

        if (personaRepository.buscarPorId(persona.getId()).isEmpty()) {
            auditoriaService.registrar(usuarioActual.getId(), "ACTUALIZAR_PERSONA", "personas",
                    "Persona no encontrada: id=" + persona.getId(),
                    ResultadoAuditoria.FALLO);
            throw new NoSuchElementException("No existe una persona con id " + persona.getId());
        }

        Persona actualizada = personaRepository.actualizar(persona);

        auditoriaService.registrar(usuarioActual.getId(), "ACTUALIZAR_PERSONA", "personas",
                "Persona actualizada: documento=" + actualizada.getDocumento() + " nombre=" + actualizada.getNombre(),
                ResultadoAuditoria.EXITO);
        return actualizada;
    }
}