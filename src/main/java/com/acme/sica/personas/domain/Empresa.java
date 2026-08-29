package com.acme.sica.personas.domain;

public class Empresa {

    private Long id;
    private String nombre;
    private String nit;

    public Empresa() {
    }

    public Empresa(Long id, String nombre, String nit) {
        this.id = id;
        this.nombre = nombre;
        this.nit = nit;
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

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }
}