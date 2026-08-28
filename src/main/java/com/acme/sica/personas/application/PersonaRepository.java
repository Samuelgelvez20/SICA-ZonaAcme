package com.acme.sica.personas.application;

import com.acme.sica.personas.domain.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository {
    Persona guardar(Persona persona);
    Persona actualizar(Persona persona);
    Optional<Persona> buscarPorDocumento(String documento);
    Optional<Persona> buscarPorId(Long id);
    List<Persona> listarPorEmpresa(Long empresaId);
}