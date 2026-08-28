package com.acme.sica;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.infrastructure.BitacoraRepositoryJdbc;
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
            BitacoraRepository bitacoraRepository = new BitacoraRepositoryJdbc();
            AuditoriaService auditoriaService = new AuditoriaService(bitacoraRepository);

            AutenticarUsuarioService autenticarUsuarioService =
                    new AutenticarUsuarioService(usuarioRepository, auditoriaService);
            AutorizarAccionService autorizarAccionService =
                    new AutorizarAccionService(auditoriaService);

            new LoginView(autenticarUsuarioService).mostrar();
        });
    }
}
