package com.acme.sica;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.infrastructure.BitacoraRepositoryJdbc;
import com.acme.sica.incidentes.application.IncidenteRepository;
import com.acme.sica.incidentes.application.RegistrarIncidenteService;
import com.acme.sica.incidentes.infrastructure.IncidenteRepositoryJdbc;
import com.acme.sica.personas.application.BloquearPersonaService;
import com.acme.sica.personas.application.CrearEmpresaService;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.infrastructure.EmpresaRepositoryJdbc;
import com.acme.sica.personas.infrastructure.PersonaRepositoryJdbc;
import com.acme.sica.reportes.application.GenerarReporteBitacoraService;
import com.acme.sica.reportes.application.GenerarReporteVisitasDentroService;
import com.acme.sica.usuarios.application.AutenticarUsuarioService;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.usuarios.infrastructure.LoginView;
import com.acme.sica.usuarios.infrastructure.PantallaPrincipal;
import com.acme.sica.usuarios.infrastructure.UsuarioRepositoryJdbc;
import com.acme.sica.visitas.application.AprobarORechazarVisitaService;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.PreRegistrarInvitadoService;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.application.RegistrarCheckOutService;
import com.acme.sica.visitas.application.RegistrarIngresoPorOlvidoService;
import com.acme.sica.visitas.application.RegistrarVisitaNoAnunciadaService;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.domain.ReglaValidacionIngreso;
import com.acme.sica.visitas.domain.ValidacionIngresoNoAnunciado;
import com.acme.sica.visitas.domain.ValidacionIngresoPorOlvido;
import com.acme.sica.visitas.infrastructure.CheckInView;
import com.acme.sica.visitas.infrastructure.CheckOutView;
import com.acme.sica.visitas.infrastructure.FuncionarioPendientesView;
import com.acme.sica.visitas.infrastructure.IngresoPorOlvidoView;
import com.acme.sica.visitas.infrastructure.NotificadorVisitasEnMemoria;
import com.acme.sica.visitas.infrastructure.PanelEsperaGuarda;
import com.acme.sica.visitas.infrastructure.VisitaRepositoryJdbc;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UsuarioRepository usuarioRepository = new UsuarioRepositoryJdbc();
            EmpresaRepository empresaRepository = new EmpresaRepositoryJdbc();
            PersonaRepository personaRepository = new PersonaRepositoryJdbc();
            BitacoraRepository bitacoraRepository = new BitacoraRepositoryJdbc();
            AuditoriaService auditoriaService = new AuditoriaService(bitacoraRepository);

            AutenticarUsuarioService autenticarUsuarioService =
                    new AutenticarUsuarioService(usuarioRepository, auditoriaService);
            AutorizarAccionService autorizarAccionService =
                    new AutorizarAccionService(auditoriaService);

            CrearEmpresaService crearEmpresaService =
                    new CrearEmpresaService(empresaRepository, autorizarAccionService, auditoriaService);
            CrearPersonaService crearPersonaService =
                    new CrearPersonaService(personaRepository, autorizarAccionService, auditoriaService);
            ActualizarPersonaService actualizarPersonaService =
                    new ActualizarPersonaService(personaRepository, autorizarAccionService, auditoriaService);
            ListarPersonasPorEmpresaService listarPersonasPorEmpresaService =
                    new ListarPersonasPorEmpresaService(personaRepository, autorizarAccionService);

            // HU-07: Pre-registro de invitado
            VisitaRepository visitaRepository = new VisitaRepositoryJdbc();
            PreRegistrarInvitadoService preRegistrarInvitadoService =
                    new PreRegistrarInvitadoService(autorizarAccionService, personaRepository,
                            visitaRepository, auditoriaService);

            // HU-08: Check-in de invitado pre-registrado
            RegistrarCheckInService registrarCheckInService =
                    new RegistrarCheckInService(autorizarAccionService, personaRepository,
                            visitaRepository, auditoriaService);

            // HU-10: Notificador Observer (UNA sola instancia compartida)
            NotificadorVisitas notificadorVisitas = new NotificadorVisitasEnMemoria();

            // HU-09: Registro de visita no anunciada (Strategy + Notificador inyectados)
            ReglaValidacionIngreso validacionNoAnunciada = new ValidacionIngresoNoAnunciado();
            RegistrarVisitaNoAnunciadaService registrarVisitaNoAnunciadaService =
                    new RegistrarVisitaNoAnunciadaService(autorizarAccionService, personaRepository,
                            crearPersonaService, visitaRepository, auditoriaService,
                            validacionNoAnunciada, notificadorVisitas);

            // HU-10: Aprobar/Rechazar visita
            AprobarORechazarVisitaService aprobarORechazarService =
                    new AprobarORechazarVisitaService(visitaRepository, autorizarAccionService,
                            auditoriaService, notificadorVisitas);

            // HU-11: Ingreso por carnet olvidado (Strategy + Notificador compartidos)
            ReglaValidacionIngreso validacionPorOlvido = new ValidacionIngresoPorOlvido();
            RegistrarIngresoPorOlvidoService registrarIngresoPorOlvidoService =
                    new RegistrarIngresoPorOlvidoService(personaRepository, autorizarAccionService,
                            visitaRepository, auditoriaService, notificadorVisitas,
                            validacionPorOlvido);

            // HU-13: Check-out normal
            RegistrarCheckOutService registrarCheckOutService =
                    new RegistrarCheckOutService(personaRepository, autorizarAccionService,
                            visitaRepository, auditoriaService);

            // HU-14: Incidentes y bloqueo de personas
            IncidenteRepository incidenteRepository = new IncidenteRepositoryJdbc();
            RegistrarIncidenteService registrarIncidenteService =
                    new RegistrarIncidenteService(autorizarAccionService, personaRepository,
                            incidenteRepository, auditoriaService);
            BloquearPersonaService bloquearPersonaService =
                    new BloquearPersonaService(autorizarAccionService, personaRepository,
                            auditoriaService);

            // HU-15: Reportes
            GenerarReporteVisitasDentroService generarReporteVisitasDentroService =
                    new GenerarReporteVisitasDentroService(autorizarAccionService, visitaRepository,
                            personaRepository, empresaRepository, usuarioRepository, auditoriaService);
            GenerarReporteBitacoraService generarReporteBitacoraService =
                    new GenerarReporteBitacoraService(autorizarAccionService, bitacoraRepository,
                            usuarioRepository, auditoriaService);

            // HU-16: Login + navegacion por rol
            new LoginView(autenticarUsuarioService).mostrar()
                    .ifPresent(usuario -> new PantallaPrincipal(
                            usuario,
                            personaRepository, empresaRepository, usuarioRepository,
                            visitaRepository, bitacoraRepository, notificadorVisitas,
                            registrarCheckInService, registrarCheckOutService,
                            registrarIngresoPorOlvidoService, preRegistrarInvitadoService,
                            aprobarORechazarService, crearPersonaService, crearEmpresaService,
                            actualizarPersonaService, listarPersonasPorEmpresaService,
                            registrarIncidenteService, bloquearPersonaService,
                            generarReporteVisitasDentroService, generarReporteBitacoraService
                    ).mostrar());
        });
    }
}