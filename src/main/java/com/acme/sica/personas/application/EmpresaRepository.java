package com.acme.sica.personas.application;

import com.acme.sica.personas.domain.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
    Empresa guardar(Empresa empresa);
    Optional<Empresa> buscarPorNit(String nit);
    Optional<Empresa> buscarPorId(Long id);
    List<Empresa> listarTodas();
}