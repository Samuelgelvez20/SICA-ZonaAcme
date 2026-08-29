package com.acme.sica.reportes.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.application.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GenerarReporteBitacoraService {

    private final AutorizarAccionService autorizarAccionService;
    private final BitacoraRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public GenerarReporteBitacoraService(AutorizarAccionService autorizarAccionService,
                                          BitacoraRepository bitacoraRepository,
                                          UsuarioRepository usuarioRepository,
                                          AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.bitacoraRepository = bitacoraRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<ReporteBitacora> ejecutar(com.acme.sica.usuarios.domain.Usuario usuarioActual,
                                           FiltrosBitacora filtros) {
        autorizarAccionService.verificar(usuarioActual, "generar_reporte");

        LocalDateTime fechaDesde = filtros.getFechaDesde() != null
                ? filtros.getFechaDesde().atStartOfDay() : null;
        LocalDateTime fechaHasta = filtros.getFechaHasta() != null
                ? filtros.getFechaHasta().plusDays(1).atStartOfDay() : null;

        var registros = bitacoraRepository.listarConFiltros(
                filtros.getUsuarioId(),
                filtros.getAccion(),
                fechaDesde,
                fechaHasta
        );

        List<ReporteBitacora> resultado = registros.stream()
                .map(reg -> {
                    String nombreUsuario = "<desconocido>";
                    if (reg.getUsuarioId() != null) {
                        nombreUsuario = usuarioRepository.buscarPorId(reg.getUsuarioId())
                                .map(u -> u.getNombre()).orElse("<desconocido>");
                    }
                    return new ReporteBitacora(
                            reg.getId(),
                            nombreUsuario,
                            reg.getAccion(),
                            reg.getEntidad(),
                            reg.getDetalle(),
                            reg.getFechaHora(),
                            reg.getResultado()
                    );
                })
                .sorted(Comparator.comparing(ReporteBitacora::getFechaHora).reversed())
                .collect(Collectors.toList());

        auditoriaService.registrar(usuarioActual.getId(), "REPORTE_BITACORA", "bitacora_auditoria",
                "registros=" + resultado.size()
                + ", fechaDesde=" + filtros.getFechaDesde()
                + ", fechaHasta=" + filtros.getFechaHasta()
                + ", usuarioId=" + filtros.getUsuarioId()
                + ", accion=" + filtros.getAccion(),
                ResultadoAuditoria.EXITO);

        return resultado;
    }
}
