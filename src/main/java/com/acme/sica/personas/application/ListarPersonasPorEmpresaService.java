package com.acme.sica.personas.application;

import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.personas.domain.Persona;

import java.util.List;

public class ListarPersonasPorEmpresaService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;

    public ListarPersonasPorEmpresaService(PersonaRepository personaRepository,
                                            AutorizarAccionService autorizarAccionService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
    }

    public List<Persona> ejecutar(Usuario usuarioActual, Long empresaId) {
        autorizarAccionService.verificar(usuarioActual, "editar_persona");
        return personaRepository.listarPorEmpresa(empresaId);
    }
}