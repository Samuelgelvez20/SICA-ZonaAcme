package com.acme.sica.usuarios.application;

import com.acme.sica.shared.CredencialesInvalidasException;
import com.acme.sica.usuarios.domain.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public class AutenticarUsuarioService {

    private static final String MENSAJE_FALLO =
            "Usuario o contraseña incorrectos, o usuario inactivo";

    private final UsuarioRepository usuarioRepository;

    public AutenticarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario autenticar(String username, String passwordPlano) {
        if (username == null || username.isBlank()
                || passwordPlano == null || passwordPlano.isEmpty()) {
            // TODO HU-05: registrar intento de login fallido en bitacora_auditoria
            //             accion = "LOGIN", resultado = "FALLO",
            //             detalle = "credenciales vacias".
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        Optional<Usuario> opt = usuarioRepository.buscarPorUsername(username.trim());

        if (opt.isEmpty()) {
            // TODO HU-05: registrar intento de login fallido en bitacora_auditoria
            //             accion = "LOGIN", resultado = "FALLO",
            //             detalle = "username=" + username + " (no existe)".
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        Usuario usuario = opt.get();

        if (!usuario.isActivo()) {
            // TODO HU-05: registrar intento de login fallido en bitacora_auditoria
            //             accion = "LOGIN", resultado = "FALLO",
            //             detalle = "username=" + username + " (inactivo)".
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        String hashCalculado = sha256Hex(passwordPlano);
        if (!hashCalculado.equalsIgnoreCase(usuario.getPasswordHash())) {
            // TODO HU-05: registrar intento de login fallido en bitacora_auditoria
            //             accion = "LOGIN", resultado = "FALLO",
            //             detalle = "username=" + username + " (password invalido)".
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        // TODO HU-05: registrar login exitoso en bitacora_auditoria
        //             accion = "LOGIN", resultado = "EXITO",
        //             usuario = usuario, detalle = "username=" + username.
        return usuario;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en el JDK", e);
        }
    }
}
