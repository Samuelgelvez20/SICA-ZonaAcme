package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.shared.CredencialesInvalidasException;
import com.acme.sica.usuarios.application.AutenticarUsuarioService;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.Optional;

public class LoginView {

    private final AutenticarUsuarioService autenticarUsuarioService;

    public LoginView(AutenticarUsuarioService autenticarUsuarioService) {
        this.autenticarUsuarioService = autenticarUsuarioService;
    }

    public Optional<Usuario> mostrar() {
        while (true) {
            String[] credenciales = pedirCredenciales();
            if (credenciales == null) {
                return Optional.empty();
            }
            String username = credenciales[0];
            String password = credenciales[1];

            try {
                Usuario usuario = autenticarUsuarioService.autenticar(username, password);
                JOptionPane.showMessageDialog(
                        null,
                        "Bienvenido, " + usuario.getNombre() + " \u2014 rol " + usuario.getRol().getNombre(),
                        "SICA - Acceso concedido",
                        JOptionPane.INFORMATION_MESSAGE);
                return Optional.of(usuario);
            } catch (CredencialesInvalidasException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "SICA - Error de autenticaci\u00f3n",
                        JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error inesperado: " + ex.getMessage(),
                        "SICA - Error",
                        JOptionPane.ERROR_MESSAGE);
                return Optional.empty();
            }
        }
    }

    private static String[] pedirCredenciales() {
        JTextField campoUsername = new JTextField();
        JPasswordField campoPassword = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsername);
        panel.add(new JLabel("Contrase\u00f1a:"));
        panel.add(campoPassword);

        int opcion = JOptionPane.showConfirmDialog(
                null,
                panel,
                "SICA - Inicio de sesi\u00f3n",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (opcion != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[]{
                campoUsername.getText(),
                new String(campoPassword.getPassword())
        };
    }
}
