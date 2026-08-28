package com.acme.sica.usuarios.domain;

import java.util.Objects;

public class Usuario {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String nombre;
    private final Rol rol;
    private final boolean activo;

    public Usuario(Long id,
                   String username,
                   String passwordHash,
                   String nombre,
                   Rol rol,
                   boolean activo) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "nombre es obligatorio");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return "Usuario{username='" + username + "', nombre='" + nombre + "', rol=" + rol.getNombre() + "}";
    }
}
