package com.acme.sica;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.infrastructure.BitacoraRepositoryJdbc;
import com.acme.sica.personas.application.CrearEmpresaService;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.infrastructure.EmpresaRepositoryJdbc;
import com.acme.sica.personas.infrastructure.PersonaRepositoryJdbc;
import com.acme.sica.usuarios.application.AutenticarUsuarioService;
import com.acme.sica.usuarios.application.AutorizarAccionService;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.usuarios.infrastructure.LoginView;
import com.acme.sica.usuarios.infrastructure.UsuarioRepositoryJdbc;
import com.acme.sica.visitas.application.PreRegistrarInvitadoService;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.infrastructure.VisitaRepositoryJdbc;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;

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

            // Smoke test temporal HU-07
            ejecutarSmokeTestHU07(autenticarUsuarioService, preRegistrarInvitadoService);

            new LoginView(autenticarUsuarioService).mostrar();
        });
    }

    private static void ejecutarSmokeTestHU07(AutenticarUsuarioService autenticarUsuarioService,
                                               PreRegistrarInvitadoService preRegistrarInvitadoService) {
        // Autenticar a funcionario1 (Laura Gómez) - password "1234"
        Usuario funcionario;
        try {
            funcionario = autenticarUsuarioService.autenticar("funcionario1", "1234");
        } catch (Exception e) {
            System.err.println("SMOKE TEST HU-07 FALLÓ: No se pudo autenticar funcionario1 - " + e.getMessage());
            return;
        }
        System.out.println("SMOKE TEST HU-07: Autenticado " + funcionario.getNombre());

        // Persona: Maria Rodríguez (documento 1009876543, id=2)
        // Empresa: TechNova S.A.S. (id=1)
        Long personaId = 2L;
        Long empresaVisitadaId = 1L;
        LocalDateTime fechaProgramada = LocalDateTime.now().plusDays(1); // mañana

        try {
            var visita = preRegistrarInvitadoService.ejecutar(funcionario, personaId, empresaVisitadaId, fechaProgramada);
            System.out.println("SMOKE TEST HU-07 ÉXITO: Visita creada con ID=" + visita.getId()
                    + ", estado=" + visita.getEstado()
                    + ", fechaHoraProgramada=" + visita.getFechaHoraProgramada());
        } catch (Exception e) {
            System.err.println("SMOKE TEST HU-07 FALLÓ: " + e.getMessage());
            e.printStackTrace();
        }

        // Probar con guarda1 (Carlos Pérez) - también tiene registrar_visita
        try {
            Usuario guarda = autenticarUsuarioService.autenticar("guarda1", "1234");
            System.out.println("SMOKE TEST HU-07: Probando con " + guarda.getNombre() + " (también tiene registrar_visita)");
            var visita2 = preRegistrarInvitadoService.ejecutar(guarda, personaId, empresaVisitadaId,
                    LocalDateTime.now().plusDays(2));
            System.out.println("SMOKE TEST HU-07 ÉXITO (guarda): Visita ID=" + visita2.getId());
        } catch (Exception e) {
            System.err.println("SMOKE TEST HU-07 FALLÓ (guarda): " + e.getMessage());
        }
    }
}