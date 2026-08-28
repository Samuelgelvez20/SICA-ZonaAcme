package com.acme.sica.personas.domain;

public class Persona {

    private Long id;
    private String nombre;
    private String documento;
    private TipoPersona tipo;
    private String fotoUrl;
    private Long empresaId;
    private Long funcionarioAnfitrionId;
    private boolean bloqueado;
    private String motivoBloqueo;

    public Persona() {
    }

    public Persona(Long id, String nombre, String documento, TipoPersona tipo,
                   String fotoUrl, Long empresaId, Long funcionarioAnfitrionId,
                   boolean bloqueado, String motivoBloqueo) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.tipo = tipo;
        this.fotoUrl = fotoUrl;
        this.empresaId = empresaId;
        this.funcionarioAnfitrionId = funcionarioAnfitrionId;
        this.bloqueado = bloqueado;
        this.motivoBloqueo = motivoBloqueo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public TipoPersona getTipo() {
        return tipo;
    }

    public void setTipo(TipoPersona tipo) {
        this.tipo = tipo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getFuncionarioAnfitrionId() {
        return funcionarioAnfitrionId;
    }

    public void setFuncionarioAnfitrionId(Long funcionarioAnfitrionId) {
        this.funcionarioAnfitrionId = funcionarioAnfitrionId;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public String getMotivoBloqueo() {
        return motivoBloqueo;
    }

    public void setMotivoBloqueo(String motivoBloqueo) {
        this.motivoBloqueo = motivoBloqueo;
    }
}