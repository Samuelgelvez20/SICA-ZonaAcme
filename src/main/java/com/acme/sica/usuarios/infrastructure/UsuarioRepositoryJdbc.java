package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.config.ConexionPostgres;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.domain.Permiso;
import com.acme.sica.usuarios.domain.Rol;
import com.acme.sica.usuarios.domain.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private static final String SQL_BUSCAR_POR_USERNAME = """
            SELECT u.id            AS usuario_id,
                   u.username      AS username,
                   u.password_hash AS password_hash,
                   u.nombre        AS nombre,
                   u.activo        AS activo,
                   r.id            AS rol_id,
                   r.nombre        AS rol_nombre,
                   p.id            AS permiso_id,
                   p.codigo        AS permiso_codigo,
                   p.descripcion   AS permiso_descripcion
              FROM usuarios u
              JOIN roles r ON r.id = u.rol_id
              LEFT JOIN rol_permisos rp ON rp.rol_id = r.id
              LEFT JOIN permisos p ON p.id = rp.permiso_id
             WHERE u.username = ?
             ORDER BY r.id, p.id
            """;

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_USERNAME)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario '" + username + "'", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT u.id            AS usuario_id,
                       u.username      AS username,
                       u.password_hash AS password_hash,
                       u.nombre        AS nombre,
                       u.activo        AS activo,
                       r.id            AS rol_id,
                       r.nombre        AS rol_nombre,
                       p.id            AS permiso_id,
                       p.codigo        AS permiso_codigo,
                       p.descripcion   AS permiso_descripcion
                  FROM usuarios u
                  JOIN roles r ON r.id = u.rol_id
                  LEFT JOIN rol_permisos rp ON rp.rol_id = r.id
                  LEFT JOIN permisos p ON p.id = rp.permiso_id
                 WHERE u.id = ?
                 ORDER BY r.id, p.id
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por id " + id, e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = """
                SELECT u.id            AS usuario_id,
                       u.username      AS username,
                       u.password_hash AS password_hash,
                       u.nombre        AS nombre,
                       u.activo        AS activo,
                       r.id            AS rol_id,
                       r.nombre        AS rol_nombre,
                       p.id            AS permiso_id,
                       p.codigo        AS permiso_codigo,
                       p.descripcion   AS permiso_descripcion
                  FROM usuarios u
                  JOIN roles r ON r.id = u.rol_id
                  LEFT JOIN rol_permisos rp ON rp.rol_id = r.id
                  LEFT JOIN permisos p ON p.id = rp.permiso_id
                 WHERE u.activo = TRUE
                 ORDER BY u.nombre
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, UsuarioEnConstruccion> porUsuario = new HashMap<>();

                while (rs.next()) {
                    long usuarioId = rs.getLong("usuario_id");
                    UsuarioEnConstruccion u = porUsuario.computeIfAbsent(usuarioId, id -> {
                        try {
                            return new UsuarioEnConstruccion(
                                    id,
                                    rs.getString("username"),
                                    rs.getString("password_hash"),
                                    rs.getString("nombre"),
                                    rs.getBoolean("activo"),
                                    rs.getLong("rol_id"),
                                    rs.getString("rol_nombre")
                            );
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    long permisoId = rs.getLong("permiso_id");
                    if (!rs.wasNull()) {
                        u.permisos.add(new Permiso(
                                permisoId,
                                rs.getString("permiso_codigo"),
                                rs.getString("permiso_descripcion")
                        ));
                    }
                }

                return porUsuario.values().stream()
                        .map(UsuarioEnConstruccion::toUsuario)
                        .toList();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar todos los usuarios", e);
        }
    }

    private static Optional<Usuario> mapear(ResultSet rs) throws SQLException {
        Map<Long, UsuarioEnConstruccion> porUsuario = new HashMap<>();

        while (rs.next()) {
            long usuarioId = rs.getLong("usuario_id");

            UsuarioEnConstruccion u = porUsuario.computeIfAbsent(usuarioId, id -> {
                try {
                    return new UsuarioEnConstruccion(
                            id,
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("nombre"),
                            rs.getBoolean("activo"),
                            rs.getLong("rol_id"),
                            rs.getString("rol_nombre")
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            long permisoId = rs.getLong("permiso_id");
            if (!rs.wasNull()) {
                u.permisos.add(new Permiso(
                        permisoId,
                        rs.getString("permiso_codigo"),
                        rs.getString("permiso_descripcion")
                ));
            }
        }

        return porUsuario.values().stream()
                .map(UsuarioEnConstruccion::toUsuario)
                .findFirst();
    }

    private static final class UsuarioEnConstruccion {
        final Long id;
        final String username;
        final String passwordHash;
        final String nombre;
        final boolean activo;
        final Long rolId;
        final String rolNombre;
        final List<Permiso> permisos = new ArrayList<>();

        UsuarioEnConstruccion(Long id,
                              String username,
                              String passwordHash,
                              String nombre,
                              boolean activo,
                              Long rolId,
                              String rolNombre) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.nombre = nombre;
            this.activo = activo;
            this.rolId = rolId;
            this.rolNombre = rolNombre;
        }

        Usuario toUsuario() {
            return new Usuario(id, username, passwordHash, nombre,
                    new Rol(rolId, rolNombre, permisos), activo);
        }
    }
}
