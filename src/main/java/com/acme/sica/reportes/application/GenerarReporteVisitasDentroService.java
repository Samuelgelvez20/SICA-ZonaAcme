package com.acme.sica.reportes.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.visitas.application.VisitaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GenerarReporteVisitasDentroService {

    private final AutorizarAccionService autorizarAccionService;
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public GenerarReporteVisitasDentroService(AutorizarAccionService autorizarAccionService,
                                               VisitaRepository visitaRepository,
                                               PersonaRepository personaRepository,
                                               EmpresaRepository empresaRepository,
                                               UsuarioRepository usuarioRepository,
                                               AuditoriaService auditoriaService) {
        this.autorizarAccionService = autorizarAccionService;
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<ReporteVisitasDentro> ejecutar(com.acme.sica.usuarios.domain.Usuario usuarioActual) {
        autorizarAccionService.verificar(usuarioActual, "generar_reporte");

        var visitas = visitaRepository.listarVisitasDentro();

        List<ReporteVisitasDentro> resultado = visitas.stream()
                .map(visita -> {
                    var personaOpt = personaRepository.buscarPorId(visita.getPersonaId());
                    String nombrePersona = personaOpt.map(p -> p.getNombre()).orElse("<desconocida>");
                    String documento = personaOpt.map(p -> p.getDocumento()).orElse("");
                    String tipo = personaOpt.map(p -> p.getTipo().name()).orElse("");

                    String nombreEmpresa = null;
                    if (personaOpt.isPresent() && personaOpt.get().getEmpresaId() != null) {
                        nombreEmpresa = empresaRepository.buscarPorId(personaOpt.get().getEmpresaId())
                                .map(e -> e.getNombre()).orElse(null);
                    }

                    String nombreGuarda = null;
                    if (visita.getGuardaId() != null) {
                        nombreGuarda = usuarioRepository.buscarPorId(visita.getGuardaId())
                                .map(u -> u.getNombre()).orElse(null);
                    }

                    return new ReporteVisitasDentro(
                            visita.getId(),
                            nombrePersona,
                            documento,
                            tipo,
                            nombreEmpresa,
                            nombreGuarda,
                            visita.getFechaHoraIngreso()
                    );
                })
                .sorted(Comparator.comparing(ReporteVisitasDentro::getNombrePersona))
                .collect(Collectors.toList());

        auditoriaService.registrar(usuarioActual.getId(), "REPORTE_VISITAS_DENTRO", "visitas",
                "registros=" + resultado.size(), ResultadoAuditoria.EXITO);

        return resultado;
    }
}
