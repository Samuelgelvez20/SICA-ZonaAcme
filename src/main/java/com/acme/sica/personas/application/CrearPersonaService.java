package com.acme.sica.personas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.shared.EntidadDuplicadaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;

public class CrearPersonaService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final AuditoriaService auditoriaService;

    public CrearPersonaService(PersonaRepository personaRepository,
                                AutorizarAccionService autorizarAccionService,
                                AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.auditoriaService = auditoriaService;
    }

    public Persona ejecutar(Usuario usuarioActual, String nombre, String documento,
                            TipoPersona tipo, String fotoUrl, Long empresaId,
                            Long funcionarioAnfitrionId) {
        autorizarAccionService.verificar(usuarioActual, "editar_persona");

        if (personaRepository.buscarPorDocumento(documento).isPresent()) {
            auditoriaService.registrar(usuarioActual.getId(), "CREAR_PERSONA", "personas",
                    "Intento de crear persona duplicada: documento=" + documento,
                    ResultadoAuditoria.FALLO);
            throw new EntidadDuplicadaException("Ya existe una persona con documento " + documento);
        }

        Persona persona = new Persona(null, nombre, documento, tipo, fotoUrl,
                empresaId, funcionarioAnfitrionId, false, null);
        Persona guardada = personaRepository.guardar(persona);

        auditoriaService.registrar(usuarioActual.getId(), "CREAR_PERSONA", "personas",
                "Persona creada: documento=" + guardada.getDocumento() + " nombre=" + guardada.getNombre(),
                ResultadoAuditoria.EXITO);
        return guardada;
    }
}