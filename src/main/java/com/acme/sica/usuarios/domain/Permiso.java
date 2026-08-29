package com.acme.sica.usuarios.domain;

import java.util.Objects;

public class Permiso {

    private final Long id;
    private final String codigo;
    private final String descripcion;

    public Permiso(Long id, String codigo, String descripcion) {
        this.id = id;
        this.codigo = Objects.requireNonNull(codigo, "codigo es obligatorio");
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permiso)) return false;
        Permiso other = (Permiso) o;
        return Objects.equals(codigo, other.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Permiso{codigo='" + codigo + "'}";
    }
}
