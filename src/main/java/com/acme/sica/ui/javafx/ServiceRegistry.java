package com.acme.sica.ui.javafx;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.infrastructure.BitacoraRepositoryJdbc;
import com.acme.sica.incidentes.application.IncidenteRepository;
import com.acme.sica.incidentes.application.RegistrarIncidenteService;
import com.acme.sica.incidentes.infrastructure.IncidenteRepositoryJdbc;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.application.BloquearPersonaService;
import com.acme.sica.personas.application.CrearEmpresaService;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.infrastructure.EmpresaRepositoryJdbc;
import com.acme.sica.personas.infrastructure.PersonaRepositoryJdbc;
import com.acme.sica.reportes.application.GenerarReporteBitacoraService;
import com.acme.sica.reportes.application.GenerarReporteVisitasDentroService;
import com.acme.sica.usuarios.application.AutenticarUsuarioService;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.infrastructure.UsuarioRepositoryJdbc;
import com.acme.sica.visitas.application.AprobarORechazarVisitaService;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.PreRegistrarInvitadoService;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.application.RegistrarCheckOutService;
import com.acme.sica.visitas.application.RegistrarIngresoPorOlvidoService;
import com.acme.sica.visitas.application.RegistrarIngresoTrabajadorService;
import com.acme.sica.visitas.application.RegistrarVisitaNoAnunciadaService;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.domain.ReglaValidacionIngreso;
import com.acme.sica.visitas.domain.ValidacionIngresoNoAnunciado;
import com.acme.sica.visitas.domain.ValidacionIngresoPorOlvido;
import com.acme.sica.visitas.infrastructure.NotificadorVisitasEnMemoria;
import com.acme.sica.visitas.infrastructure.VisitaRepositoryJdbc;

/**
 * Contenedor central de inyección de dependencias para los servicios de SICA.
 */
public class ServiceRegistry {

    private static ServiceRegistry instance;

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PersonaRepository personaRepository;
    private final BitacoraRepository bitacoraRepository;
    private final IncidenteRepository incidenteRepository;
    private final VisitaRepository visitaRepository;
    private final NotificadorVisitas notificadorVisitas;

    private final AuditoriaService auditoriaService;
    private final AutenticarUsuarioService autenticarUsuarioService;
    private final AutorizarAccionService autorizarAccionService;
    private final CrearEmpresaService crearEmpresaService;
    private final CrearPersonaService crearPersonaService;
    private final ActualizarPersonaService actualizarPersonaService;
    private final ListarPersonasPorEmpresaService listarPersonasPorEmpresaService;
    private final BloquearPersonaService bloquearPersonaService;
    private final RegistrarIncidenteService registrarIncidenteService;
    private final PreRegistrarInvitadoService preRegistrarInvitadoService;
    private final AprobarORechazarVisitaService aprobarORechazarService;
    private final RegistrarCheckInService registrarCheckInService;
    private final RegistrarCheckOutService registrarCheckOutService;
    private final RegistrarIngresoPorOlvidoService registrarIngresoPorOlvidoService;
    private final RegistrarVisitaNoAnunciadaService registrarVisitaNoAnunciadaService;
    private final RegistrarIngresoTrabajadorService registrarIngresoTrabajadorService;
    private final GenerarReporteVisitasDentroService generarReporteVisitasDentroService;
    private final GenerarReporteBitacoraService generarReporteBitacoraService;

    private ServiceRegistry() {
        this.usuarioRepository = new UsuarioRepositoryJdbc();
        this.empresaRepository = new EmpresaRepositoryJdbc();
        this.personaRepository = new PersonaRepositoryJdbc();
        this.bitacoraRepository = new BitacoraRepositoryJdbc();
        this.incidenteRepository = new IncidenteRepositoryJdbc();
        this.visitaRepository = new VisitaRepositoryJdbc();
        this.notificadorVisitas = new NotificadorVisitasEnMemoria();

        this.auditoriaService = new AuditoriaService(bitacoraRepository);
        this.autenticarUsuarioService = new AutenticarUsuarioService(usuarioRepository, auditoriaService);
        this.autorizarAccionService = new AutorizarAccionService(auditoriaService);

        this.crearEmpresaService = new CrearEmpresaService(empresaRepository, autorizarAccionService, auditoriaService);
        this.crearPersonaService = new CrearPersonaService(personaRepository, autorizarAccionService, auditoriaService);
        this.actualizarPersonaService = new ActualizarPersonaService(personaRepository, autorizarAccionService, auditoriaService);
        this.listarPersonasPorEmpresaService = new ListarPersonasPorEmpresaService(personaRepository, autorizarAccionService, auditoriaService);

        this.preRegistrarInvitadoService = new PreRegistrarInvitadoService(
                autorizarAccionService, personaRepository, visitaRepository, auditoriaService);
        this.registrarCheckInService = new RegistrarCheckInService(
                autorizarAccionService, personaRepository, visitaRepository, auditoriaService);

        ReglaValidacionIngreso validacionNoAnunciada = new ValidacionIngresoNoAnunciado();
        this.registrarVisitaNoAnunciadaService = new RegistrarVisitaNoAnunciadaService(
                autorizarAccionService, personaRepository, crearPersonaService, visitaRepository,
                auditoriaService, validacionNoAnunciada, notificadorVisitas);

        this.aprobarORechazarService = new AprobarORechazarVisitaService(
                visitaRepository, autorizarAccionService, auditoriaService, notificadorVisitas);

        ReglaValidacionIngreso validacionPorOlvido = new ValidacionIngresoPorOlvido();
        this.registrarIngresoPorOlvidoService = new RegistrarIngresoPorOlvidoService(
                personaRepository, autorizarAccionService, visitaRepository, auditoriaService,
                notificadorVisitas, validacionPorOlvido);

        this.registrarCheckOutService = new RegistrarCheckOutService(
                personaRepository, autorizarAccionService, visitaRepository, auditoriaService);

        this.registrarIngresoTrabajadorService = new RegistrarIngresoTrabajadorService(
                personaRepository, autorizarAccionService, visitaRepository, auditoriaService);

        this.registrarIncidenteService = new RegistrarIncidenteService(
                autorizarAccionService, personaRepository, incidenteRepository, auditoriaService);

        this.bloquearPersonaService = new BloquearPersonaService(
                autorizarAccionService, personaRepository, auditoriaService);

        this.generarReporteVisitasDentroService = new GenerarReporteVisitasDentroService(
                autorizarAccionService, visitaRepository, personaRepository, empresaRepository,
                usuarioRepository, auditoriaService);

        this.generarReporteBitacoraService = new GenerarReporteBitacoraService(
                autorizarAccionService, bitacoraRepository, usuarioRepository, auditoriaService);
    }

    public static synchronized ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    public UsuarioRepository getUsuarioRepository() { return usuarioRepository; }
    public EmpresaRepository getEmpresaRepository() { return empresaRepository; }
    public PersonaRepository getPersonaRepository() { return personaRepository; }
    public BitacoraRepository getBitacoraRepository() { return bitacoraRepository; }
    public IncidenteRepository getIncidenteRepository() { return incidenteRepository; }
    public VisitaRepository getVisitaRepository() { return visitaRepository; }
    public NotificadorVisitas getNotificadorVisitas() { return notificadorVisitas; }
    public AuditoriaService getAuditoriaService() { return auditoriaService; }
    public AutenticarUsuarioService getAutenticarUsuarioService() { return autenticarUsuarioService; }
    public AutorizarAccionService getAutorizarAccionService() { return autorizarAccionService; }
    public CrearEmpresaService getCrearEmpresaService() { return crearEmpresaService; }
    public CrearPersonaService getCrearPersonaService() { return crearPersonaService; }
    public ActualizarPersonaService getActualizarPersonaService() { return actualizarPersonaService; }
    public ListarPersonasPorEmpresaService getListarPersonasPorEmpresaService() { return listarPersonasPorEmpresaService; }
    public BloquearPersonaService getBloquearPersonaService() { return bloquearPersonaService; }
    public RegistrarIncidenteService getRegistrarIncidenteService() { return registrarIncidenteService; }
    public PreRegistrarInvitadoService getPreRegistrarInvitadoService() { return preRegistrarInvitadoService; }
    public AprobarORechazarVisitaService getAprobarORechazarService() { return aprobarORechazarService; }
    public RegistrarCheckInService getRegistrarCheckInService() { return registrarCheckInService; }
    public RegistrarCheckOutService getRegistrarCheckOutService() { return registrarCheckOutService; }
    public RegistrarIngresoPorOlvidoService getRegistrarIngresoPorOlvidoService() { return registrarIngresoPorOlvidoService; }
    public RegistrarVisitaNoAnunciadaService getRegistrarVisitaNoAnunciadaService() { return registrarVisitaNoAnunciadaService; }
    public RegistrarIngresoTrabajadorService getRegistrarIngresoTrabajadorService() { return registrarIngresoTrabajadorService; }
    public GenerarReporteVisitasDentroService getGenerarReporteVisitasDentroService() { return generarReporteVisitasDentroService; }
    public GenerarReporteBitacoraService getGenerarReporteBitacoraService() { return generarReporteBitacoraService; }
}
