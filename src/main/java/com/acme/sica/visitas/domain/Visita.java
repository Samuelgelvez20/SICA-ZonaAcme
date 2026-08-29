package com.acme.sica.visitas.domain;

import java.time.LocalDateTime;

public class Visita {

    private Long id;
    private Long personaId;
    private Long guardaId;
    private Long funcionarioId;
    private Long empresaVisitadaId;
    private LocalDateTime fechaHoraProgramada;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraSalida;
    private EstadoVisita estado;
    private String motivo;

    public Visita() {
    }

    public Visita(Long id, Long personaId, Long guardaId, Long funcionarioId,
                  Long empresaVisitadaId, LocalDateTime fechaHoraProgramada,
                  LocalDateTime fechaHoraIngreso, LocalDateTime fechaHoraSalida,
                  EstadoVisita estado, String motivo) {
        this.id = id;
        this.personaId = personaId;
        this.guardaId = guardaId;
        this.funcionarioId = funcionarioId;
        this.empresaVisitadaId = empresaVisitadaId;
        this.fechaHoraProgramada = fechaHoraProgramada;
        this.fechaHoraIngreso = fechaHoraIngreso;
        this.fechaHoraSalida = fechaHoraSalida;
        this.estado = estado;
        this.motivo = motivo;
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

    public Long getGuardaId() {
        return guardaId;
    }

    public void setGuardaId(Long guardaId) {
        this.guardaId = guardaId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public Long getEmpresaVisitadaId() {
        return empresaVisitadaId;
    }

    public void setEmpresaVisitadaId(Long empresaVisitadaId) {
        this.empresaVisitadaId = empresaVisitadaId;
    }

    public LocalDateTime getFechaHoraProgramada() {
        return fechaHoraProgramada;
    }

    public void setFechaHoraProgramada(LocalDateTime fechaHoraProgramada) {
        this.fechaHoraProgramada = fechaHoraProgramada;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public EstadoVisita getEstado() {
        return estado;
    }

    public void setEstado(EstadoVisita estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "Visita{" +
                "id=" + id +
                ", personaId=" + personaId +
                ", guardaId=" + guardaId +
                ", funcionarioId=" + funcionarioId +
                ", empresaVisitadaId=" + empresaVisitadaId +
                ", fechaHoraProgramada=" + fechaHoraProgramada +
                ", fechaHoraIngreso=" + fechaHoraIngreso +
                ", fechaHoraSalida=" + fechaHoraSalida +
                ", estado=" + estado +
                ", motivo='" + motivo + '\'' +
                '}';
    }
}