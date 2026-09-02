package com.acme.sica.personas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.shared.EntidadDuplicadaException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.personas.domain.Empresa;

public class CrearEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final AutorizarAccionService autorizarAccionService;
    private final AuditoriaService auditoriaService;

    public CrearEmpresaService(EmpresaRepository empresaRepository,
                                AutorizarAccionService autorizarAccionService,
                                AuditoriaService auditoriaService) {
        this.empresaRepository = empresaRepository;
        this.autorizarAccionService = autorizarAccionService;
        this.auditoriaService = auditoriaService;
    }

    public Empresa ejecutar(Usuario usuarioActual, String nombre, String nit) {
        autorizarAccionService.verificar(usuarioActual, "editar_persona");

        if (empresaRepository.buscarPorNit(nit).isPresent()) {
            auditoriaService.registrar(usuarioActual.getId(), "CREAR_EMPRESA", "empresas",
                    "Intento de crear empresa duplicada: nit=" + nit, ResultadoAuditoria.FALLO);
            throw new EntidadDuplicadaException("Ya existe una empresa con nit " + nit);
        }

        Empresa empresa = new Empresa(null, nombre, nit);
        Empresa guardada = empresaRepository.guardar(empresa);

        auditoriaService.registrar(usuarioActual.getId(), "CREAR_EMPRESA", "empresas",
                "Empresa creada: nombre=" + guardada.getNombre() + " nit=" + guardada.getNit(),
                ResultadoAuditoria.EXITO);
        return guardada;
    }
}