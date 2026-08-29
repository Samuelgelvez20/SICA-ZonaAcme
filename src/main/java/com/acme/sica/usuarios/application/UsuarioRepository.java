package com.acme.sica.usuarios.application;

import com.acme.sica.usuarios.domain.Usuario;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorId(Long id);
}
