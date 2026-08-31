package com.acme.sica.reportes.application;

import com.acme.sica.auditoria.domain.ResultadoAuditoria;

import java.time.LocalDateTime;

public class ReporteBitacora {

    private Long id;
    private String nombreUsuario;
    private String accion;
    private String entidad;
    private String detalle;
    private LocalDateTime fechaHora;
    private ResultadoAuditoria resultado;

    public ReporteBitacora(Long id, String nombreUsuario, String accion, String entidad,
                            String detalle, LocalDateTime fechaHora, ResultadoAuditoria resultado) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.accion = accion;
        this.entidad = entidad;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
        this.resultado = resultado;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public ResultadoAuditoria getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return "ReporteBitacora{" +
                "id=" + id +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", accion='" + accion + '\'' +
                ", entidad='" + entidad + '\'' +
                ", detalle='" + detalle + '\'' +
                ", fechaHora=" + fechaHora +
                ", resultado=" + resultado +
                '}';
    }
}
