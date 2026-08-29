package com.acme.sica.visitas.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.shared.PersonaNoEncontradaException;
import com.acme.sica.shared.ValidacionIngresoException;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.ReglaValidacionIngreso;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.visitas.domain.VisitaFactory;

public class RegistrarVisitaNoAnunciadaService {

    private final AutorizarAccionService autorizarAccionService;
    private final PersonaRepository personaRepository;
    private final CrearPersonaService crearPersonaService;
    private final VisitaRepository visitaRepository;
    private final AuditoriaService auditoriaService;
    private final ReglaValidacionIngreso reglaValidacionIngreso;

    public RegistrarVisitaNoAnunciadaService(AutorizarAccionService autorizarAccionService,
                                             PersonaRepository personaRepository,
                                             CrearPersonaService crearPersonaService,
                                             VisitaRepository visitaRepository,
                                             AuditoriaService auditoriaService,
                                             ReglaValidacionIngreso reglaValidacionIngreso) {
        this.autorizarAccionService = autorizarAccionService;
        this.personaRepository = personaRepository;
        this.crearPersonaService = crearPersonaService;
        this.visitaRepository = visitaRepository;
        this.auditoriaService = auditoriaService;
        this.reglaValidacionIngreso = reglaValidacionIngreso;
    }

    public Visita registrar(Usuario usuarioActual, String documento,
                            String nombreSiNoExiste, TipoPersona tipoSiNoExiste,
                            String fotoUrlSiNoExiste, Long empresaIdSiNoExiste,
                            Long funcionarioId, Long empresaVisitadaId) {

        // a) Autorización
        autorizarAccionService.verificar(usuarioActual, "registrar_visita");

        // b) Buscar persona por documento
        Persona persona;
        var personaOpt = personaRepository.buscarPorDocumento(documento);
        if (personaOpt.isEmpty()) {
            // c) No existe: crear reutilizando CrearPersonaService
            persona = crearPersonaService.ejecutar(usuarioActual, nombreSiNoExiste, documento,
                    tipoSiNoExiste, fotoUrlSiNoExiste, empresaIdSiNoExiste, funcionarioId);
        } else {
            // c) Existe: usarla tal cual, ignorar parámetros "SiNoExiste"
            persona = personaOpt.get();
        }

        // d) Aplicar validación Strategy
        try {
            reglaValidacionIngreso.validar(persona);
        } catch (ValidacionIngresoException e) {
            String detalle = "Intento de registro no anunciado: persona='" + documento +
                    "' rechazada por validaci\u00f3n: " + e.getMessage();
            auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_VISITA_NO_ANUNCIADA",
                    "visitas", detalle, ResultadoAuditoria.FALLO);
            throw e;
        }

        // e) Crear visita vía Factory
        Visita visita = VisitaFactory.crearNoAnunciada(persona.getId(), usuarioActual.getId(),
                funcionarioId, empresaVisitadaId);

        // f) Guardar y auditar éxito
        Visita guardada = visitaRepository.guardar(visita);

        String detalleExito = "personaId=" + persona.getId() + ", persona=" + persona.getNombre() +
                ", documento=" + documento + ", funcionarioId=" + funcionarioId +
                ", empresaVisitadaId=" + empresaVisitadaId;
        auditoriaService.registrar(usuarioActual.getId(), "REGISTRAR_VISITA_NO_ANUNCIADA",
                "visitas", detalleExito, ResultadoAuditoria.EXITO);

        return guardada;
    }
}