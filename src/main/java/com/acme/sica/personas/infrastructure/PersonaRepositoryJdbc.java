package com.acme.sica.personas.infrastructure;

import com.acme.sica.config.ConexionPostgres;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaRepositoryJdbc implements PersonaRepository {

    @Override
    public Persona guardar(Persona persona) {
        String sql = """
                INSERT INTO personas (nombre, documento, tipo, foto_url, empresa_id,
                                      funcionario_anfitrion_id, bloqueado, motivo_bloqueo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDocumento());
            stmt.setString(3, persona.getTipo().name());
            if (persona.getFotoUrl() != null) {
                stmt.setString(4, persona.getFotoUrl());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }
            if (persona.getEmpresaId() != null) {
                stmt.setLong(5, persona.getEmpresaId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            if (persona.getFuncionarioAnfitrionId() != null) {
                stmt.setLong(6, persona.getFuncionarioAnfitrionId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setBoolean(7, persona.isBloqueado());
            if (persona.getMotivoBloqueo() != null) {
                stmt.setString(8, persona.getMotivoBloqueo());
            } else {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    persona.setId(rs.getLong("id"));
                }
            }
            return persona;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar persona", e);
        }
    }

    @Override
    public Persona actualizar(Persona persona) {
        String sql = """
                UPDATE personas
                SET nombre = ?, documento = ?, tipo = ?, foto_url = ?, empresa_id = ?,
                    funcionario_anfitrion_id = ?, bloqueado = ?, motivo_bloqueo = ?
                WHERE id = ?
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDocumento());
            stmt.setString(3, persona.getTipo().name());
            if (persona.getFotoUrl() != null) {
                stmt.setString(4, persona.getFotoUrl());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }
            if (persona.getEmpresaId() != null) {
                stmt.setLong(5, persona.getEmpresaId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            if (persona.getFuncionarioAnfitrionId() != null) {
                stmt.setLong(6, persona.getFuncionarioAnfitrionId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setBoolean(7, persona.isBloqueado());
            if (persona.getMotivoBloqueo() != null) {
                stmt.setString(8, persona.getMotivoBloqueo());
            } else {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }
            stmt.setLong(9, persona.getId());

            stmt.executeUpdate();
            return persona;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar persona", e);
        }
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String documento) {
        String sql = """
                SELECT id, nombre, documento, tipo, foto_url, empresa_id,
                       funcionario_anfitrion_id, bloqueado, motivo_bloqueo
                FROM personas WHERE documento = ?
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPersona(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por documento", e);
        }
    }

    @Override
    public Optional<Persona> buscarPorId(Long id) {
        String sql = """
                SELECT id, nombre, documento, tipo, foto_url, empresa_id,
                       funcionario_anfitrion_id, bloqueado, motivo_bloqueo
                FROM personas WHERE id = ?
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPersona(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por id", e);
        }
    }

    @Override
    public List<Persona> listarPorEmpresa(Long empresaId) {
        String sql = """
                SELECT id, nombre, documento, tipo, foto_url, empresa_id,
                       funcionario_anfitrion_id, bloqueado, motivo_bloqueo
                FROM personas WHERE empresa_id = ?
                ORDER BY nombre
                """;
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, empresaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapPersona(rs));
                }
            }
            return personas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar personas por empresa", e);
        }
    }

    private Persona mapPersona(ResultSet rs) throws SQLException {
        Persona persona = new Persona();
        persona.setId(rs.getLong("id"));
        persona.setNombre(rs.getString("nombre"));
        persona.setDocumento(rs.getString("documento"));
        persona.setTipo(TipoPersona.valueOf(rs.getString("tipo")));
        persona.setFotoUrl(rs.getString("foto_url"));
        Long empresaId = rs.getLong("empresa_id");
        if (!rs.wasNull()) {
            persona.setEmpresaId(empresaId);
        }
        Long funcionarioId = rs.getLong("funcionario_anfitrion_id");
        if (!rs.wasNull()) {
            persona.setFuncionarioAnfitrionId(funcionarioId);
        }
        persona.setBloqueado(rs.getBoolean("bloqueado"));
        persona.setMotivoBloqueo(rs.getString("motivo_bloqueo"));
        return persona;
    }
}