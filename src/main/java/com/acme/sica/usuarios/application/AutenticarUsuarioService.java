package com.acme.sica.usuarios.application;

import com.acme.sica.auditoria.application.AuditoriaService;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
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
    private final AuditoriaService auditoriaService;

    public AutenticarUsuarioService(UsuarioRepository usuarioRepository,
                                     AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public Usuario autenticar(String username, String passwordPlano) {
        if (username == null || username.isBlank()
                || passwordPlano == null || passwordPlano.isEmpty()) {
            auditoriaService.registrar(null, "LOGIN", "usuarios",
                    "Intento de login: credenciales vacías",
                    ResultadoAuditoria.FALLO);
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        String usernameTrim = username.trim();
        Optional<Usuario> opt = usuarioRepository.buscarPorUsername(usernameTrim);

        if (opt.isEmpty()) {
            auditoriaService.registrar(null, "LOGIN", "usuarios",
                    "Intento de login: usuario '" + usernameTrim + "' no existe",
                    ResultadoAuditoria.FALLO);
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        Usuario usuario = opt.get();

        if (!usuario.isActivo()) {
            auditoriaService.registrar(usuario.getId(), "LOGIN", "usuarios",
                    "Intento de login: usuario '" + usernameTrim + "' está inactivo",
                    ResultadoAuditoria.FALLO);
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        String hashCalculado = sha256Hex(passwordPlano);
        if (!hashCalculado.equalsIgnoreCase(usuario.getPasswordHash())) {
            auditoriaService.registrar(usuario.getId(), "LOGIN", "usuarios",
                    "Intento de login: password incorrecto para '" + usernameTrim + "'",
                    ResultadoAuditoria.FALLO);
            throw new CredencialesInvalidasException(MENSAJE_FALLO);
        }

        auditoriaService.registrar(usuario.getId(), "LOGIN", "usuarios",
                "Login exitoso para '" + usernameTrim + "'",
                ResultadoAuditoria.EXITO);
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
