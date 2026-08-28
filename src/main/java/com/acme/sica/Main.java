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
import com.acme.sica.usuarios.infrastructure.LoginView;
import com.acme.sica.usuarios.infrastructure.UsuarioRepositoryJdbc;

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

            new LoginView(autenticarUsuarioService).mostrar();
        });
    }
}
