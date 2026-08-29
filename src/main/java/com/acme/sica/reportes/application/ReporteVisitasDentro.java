package com.acme.sica.reportes.application;

import java.time.LocalDateTime;

public class ReporteVisitasDentro {

    private Long visitaId;
    private String nombrePersona;
    private String documentoPersona;
    private String tipoPersona;
    private String nombreEmpresa;
    private String nombreGuarda;
    private LocalDateTime fechaHoraIngreso;

    public ReporteVisitasDentro(Long visitaId, String nombrePersona, String documentoPersona,
                                 String tipoPersona, String nombreEmpresa, String nombreGuarda,
                                 LocalDateTime fechaHoraIngreso) {
        this.visitaId = visitaId;
        this.nombrePersona = nombrePersona;
        this.documentoPersona = documentoPersona;
        this.tipoPersona = tipoPersona;
        this.nombreEmpresa = nombreEmpresa;
        this.nombreGuarda = nombreGuarda;
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    public Long getVisitaId() {
        return visitaId;
    }

    public void setVisitaId(Long visitaId) {
        this.visitaId = visitaId;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    public String getDocumentoPersona() {
        return documentoPersona;
    }

    public void setDocumentoPersona(String documentoPersona) {
        this.documentoPersona = documentoPersona;
    }

    public String getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNombreGuarda() {
        return nombreGuarda;
    }

    public void setNombreGuarda(String nombreGuarda) {
        this.nombreGuarda = nombreGuarda;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    @Override
    public String toString() {
        return "ReporteVisitasDentro{" +
                "visitaId=" + visitaId +
                ", nombrePersona='" + nombrePersona + '\'' +
                ", documentoPersona='" + documentoPersona + '\'' +
                ", tipoPersona='" + tipoPersona + '\'' +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", nombreGuarda='" + nombreGuarda + '\'' +
                ", fechaHoraIngreso=" + fechaHoraIngreso +
                '}';
    }
}
