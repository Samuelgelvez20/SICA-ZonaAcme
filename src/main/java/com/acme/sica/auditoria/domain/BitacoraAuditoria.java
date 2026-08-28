package com.acme.sica.auditoria.domain;

import java.time.LocalDateTime;

public class BitacoraAuditoria {

    private Long id;
    private Long usuarioId;
    private String accion;
    private String entidad;
    private String detalle;
    private LocalDateTime fechaHora;
    private ResultadoAuditoria resultado;

    public BitacoraAuditoria() {
    }

    public BitacoraAuditoria(Long usuarioId, String accion, String entidad,
                              String detalle, LocalDateTime fechaHora,
                              ResultadoAuditoria resultado) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
        this.resultado = resultado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public ResultadoAuditoria getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoAuditoria resultado) {
        this.resultado = resultado;
    }
}