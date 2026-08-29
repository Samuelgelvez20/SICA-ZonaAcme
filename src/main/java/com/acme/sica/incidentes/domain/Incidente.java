package com.acme.sica.incidentes.domain;

import java.time.LocalDateTime;

public class Incidente {

    private Long id;
    private Long personaId;
    private Long usuarioId;
    private String tipo;
    private String descripcion;
    private LocalDateTime fechaHora;

    public Incidente() {
    }

    public Incidente(Long id, Long personaId, Long usuarioId, String tipo,
                     String descripcion, LocalDateTime fechaHora) {
        this.id = id;
        this.personaId = personaId;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", personaId=" + personaId +
                ", usuarioId=" + usuarioId +
                ", tipo='" + tipo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
