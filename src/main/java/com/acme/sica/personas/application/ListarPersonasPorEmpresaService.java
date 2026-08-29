package com.acme.sica.personas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.personas.domain.Persona;

import java.util.List;

public class ListarPersonasPorEmpresaService {

    private final PersonaRepository personaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final AuditoriaService auditoriaService;

    public ListarPersonasPorEmpresaService(PersonaRepository personaRepository,
                                            AutorizarAccionService autorizarAccionService,
                                            AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.auditoriaService = auditoriaService;
    }

    public List<Persona> ejecutar(Usuario usuarioActual, Long empresaId) {
        autorizarAccionService.verificar(usuarioActual, "editar_persona");
        List<Persona> personas = personaRepository.listarPorEmpresa(empresaId);
        auditoriaService.registrar(usuarioActual.getId(), "LISTAR_PERSONAS_POR_EMPRESA", "personas",
                "empresaId=" + personas.size() + " personas encontradas",
                ResultadoAuditoria.EXITO);
        return personas;
    }
}