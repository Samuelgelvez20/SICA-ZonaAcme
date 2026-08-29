package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.shared.AccesoDenegadoException;
import com.acme.sica.shared.VisitaNoDecidibleException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.EstadoVisita;
import com.acme.sica.visitas.domain.Visita;

public class AprobarORechazarVisitaService {

    private final VisitaRepository visitaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final AuditoriaService auditoriaService;
    private final NotificadorVisitas notificadorVisitas;

    public AprobarORechazarVisitaService(VisitaRepository visitaRepository,
                                         AutorizarAccionService autorizarAccionService,
                                         AuditoriaService auditoriaService,
                                         NotificadorVisitas notificadorVisitas) {
        this.visitaRepository = visitaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.auditoriaService = auditoriaService;
        this.notificadorVisitas = notificadorVisitas;
    }

    public Visita aprobar(Usuario usuarioActual, Long visitaId) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "aprobar_visita");

        // b) Buscar visita y validar estado
        Visita visita = visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new VisitaNoDecidibleException("Visita no encontrada: " + visitaId));

        if (visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION
                && visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION_OLVIDO) {
            throw VisitaNoDecidibleException.estadoNoValido(visitaId, visita.getEstado().name());
        }

        // c) Cambiar estado a APROBADA
        visita.setEstado(EstadoVisita.APROBADA);
        Visita actualizada = visitaRepository.actualizar(visita);

        // d) Auditar éxito
        String detalle = "visitaId=" + visitaId + ", personaId=" + visita.getPersonaId() +
                ", aprobadaPor=" + usuarioActual.getId() + " (" + usuarioActual.getNombre() + ")";
        auditoriaService.registrar(usuarioActual.getId(), "APROBAR_VISITA", "visitas",
                detalle, ResultadoAuditoria.EXITO);

        // e) Notificar DESPUÉS de guardar en BD
        notificadorVisitas.notificarDecisionTomada(actualizada);

        return actualizada;
    }

    public Visita rechazar(Usuario usuarioActual, Long visitaId, String motivo) {
        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "aprobar_visita"); // mismo permiso

        // b) Buscar visita y validar estado
        Visita visita = visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new VisitaNoDecidibleException("Visita no encontrada: " + visitaId));

        if (visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION
                && visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION_OLVIDO) {
            throw VisitaNoDecidibleException.estadoNoValido(visitaId, visita.getEstado().name());
        }

        // c) Cambiar estado a RECHAZADA y guardar motivo
        visita.setEstado(EstadoVisita.RECHAZADA);
        visita.setMotivo(motivo);
        Visita actualizada = visitaRepository.actualizar(visita);

        // d) Auditar éxito
        String detalle = "visitaId=" + visitaId + ", personaId=" + visita.getPersonaId() +
                ", rechazadaPor=" + usuarioActual.getId() + " (" + usuarioActual.getNombre() + ")" +
                ", motivo=" + motivo;
        auditoriaService.registrar(usuarioActual.getId(), "RECHAZAR_VISITA", "visitas",
                detalle, ResultadoAuditoria.EXITO);

        // e) Notificar DESPUÉS de guardar en BD
        notificadorVisitas.notificarDecisionTomada(actualizada);

        return actualizada;
    }
}