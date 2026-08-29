package com.acme.sica.incidentes.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.incidentes.domain.Incidente;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;

import java.time.LocalDateTime;

public class RegistrarIncidenteService {

    private final AutorizarAccionService autorizarAccionService;
    private final PersonaRepository personaRepository;
    private final IncidenteRepository incidenteRepository;
    private final AuditoriaService auditoriaService;

    public RegistrarIncidenteService(AutorizarAccionService autorizarAccionService,
                                      PersonaRepository personaRepository,
                                      IncidenteRepository incidenteRepository,
                                      AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.personaRepository = personaRepository;
        this.incidenteRepository = incidenteRepository;
        this.auditoriaService = auditoriaService;
    }

    public Incidente ejecutar(Usuario usuarioActual, Long personaId, String tipo, String descripcion) {
        // a) Autorizacion
        autorizarAccionService.verificar(usuarioActual, "registrar_incidente");

        // b) Validar personaId
        if (personaId == null) {
            auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_INCIDENTE", "incidentes",
                    "Intento de registrar incidente: personaId es null",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("El ID de la persona es obligatorio");
        }

        // c) Buscar persona
        var personaOpt = personaRepository.buscarPorId(personaId);
        if (personaOpt.isEmpty()) {
            auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_INCIDENTE", "incidentes",
                    "Intento de registrar incidente: personaId=" + personaId + " no encontrada",
                    ResultadoAuditoria.FALLO);
            throw PersonaNoEncontradaException.porId(personaId);
        }

        // d) Validar tipo
        if (tipo == null || tipo.isBlank()) {
            auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_INCIDENTE", "incidentes",
                    "Intento de registrar incidente: tipo vacio",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("El tipo del incidente es obligatorio");
        }

        // e) Validar descripcion
        if (descripcion == null || descripcion.isBlank()) {
            auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_INCIDENTE", "incidentes",
                    "Intento de registrar incidente: descripcion vacia",
                    ResultadoAuditoria.FALLO);
            throw new IllegalArgumentException("La descripcion del incidente es obligatoria");
        }

        // f) Crear incidente
        Incidente incidente = new Incidente(null, personaId, usuarioActual.getId(),
                tipo, descripcion, LocalDateTime.now());

        // g) Persistir
        Incidente guardado = incidenteRepository.guardar(incidente);

        // h) Auditar exito
        String detalle = "incidenteId=" + guardado.getId()
                + ", personaId=" + personaId
                + ", tipo=" + tipo
                + ", usuarioReporta=" + usuarioActual.getUsername();
        auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_INCIDENTE", "incidentes",
                detalle, ResultadoAuditoria.EXITO);

        // i) Retornar
        return guardado;
    }
}
