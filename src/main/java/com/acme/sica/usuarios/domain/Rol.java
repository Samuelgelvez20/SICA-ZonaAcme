package com.acme.sica.usuarios.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Rol {

    private final Long id;
    private final String nombre;
    private final List<Permiso> permisos;

    public Rol(Long id, String nombre, List<Permiso> permisos) {
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
        this.permisos = permisos == null ? List.of() : List.copyOf(permisos);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Permiso> getPermisos() {
        return Collections.unmodifiableList(permisos);
    }

    public boolean tienePermiso(String codigo) {
        if (codigo == null) {
            return false;
        }
        return permisos.stream()
                .anyMatch(p -> codigo.equals(p.getCodigo()));
    }

    @Override
    public String toString() {
        return "Rol{nombre='" + nombre + "', permisos=" + permisos.size() + "}";
    }
}
