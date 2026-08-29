package com.acme.sica.reportes.application;

import java.time.LocalDate;

public class FiltrosBitacora {

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Long usuarioId;
    private String accion;

    public FiltrosBitacora() {
    }

    public FiltrosBitacora(LocalDate fechaDesde, LocalDate fechaHasta, Long usuarioId, String accion) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.usuarioId = usuarioId;
        this.accion = accion;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
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
}
