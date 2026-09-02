package com.acme.sica.visitas.infrastructure;

import com.acme.sica.config.ConexionPostgres;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.domain.EstadoVisita;
import com.acme.sica.visitas.domain.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitaRepositoryJdbc implements VisitaRepository {

    @Override
    public Visita guardar(Visita visita) {
        String sql = """
                INSERT INTO visitas (persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                                     fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                                     estado, motivo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, visita.getPersonaId());
            setNullableLong(stmt, 2, visita.getGuardaId());
            setNullableLong(stmt, 3, visita.getFuncionarioId());
            setNullableLong(stmt, 4, visita.getEmpresaVisitadaId());
            setNullableTimestamp(stmt, 5, visita.getFechaHoraProgramada());
            setNullableTimestamp(stmt, 6, visita.getFechaHoraIngreso());
            setNullableTimestamp(stmt, 7, visita.getFechaHoraSalida());
            stmt.setString(8, visita.getEstado().name());
            setNullableString(stmt, 9, visita.getMotivo());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    visita.setId(rs.getLong("id"));
                }
            }
            return visita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar visita", e);
        }
    }

    @Override
    public Visita actualizar(Visita visita) {
        String sql = """
                UPDATE visitas
                SET guarda_id = ?, funcionario_id = ?, empresa_visitada_id = ?,
                    fecha_hora_programada = ?, fecha_hora_ingreso = ?, fecha_hora_salida = ?,
                    estado = ?, motivo = ?
                WHERE id = ?
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setNullableLong(stmt, 1, visita.getGuardaId());
            setNullableLong(stmt, 2, visita.getFuncionarioId());
            setNullableLong(stmt, 3, visita.getEmpresaVisitadaId());
            setNullableTimestamp(stmt, 4, visita.getFechaHoraProgramada());
            setNullableTimestamp(stmt, 5, visita.getFechaHoraIngreso());
            setNullableTimestamp(stmt, 6, visita.getFechaHoraSalida());
            stmt.setString(7, visita.getEstado().name());
            setNullableString(stmt, 8, visita.getMotivo());
            stmt.setLong(9, visita.getId());

            stmt.executeUpdate();
            return visita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar visita", e);
        }
    }

    @Override
    public Optional<Visita> buscarVisitaAprobadaPendienteDeIngreso(Long personaId) {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo
                FROM visitas
                WHERE persona_id = ? AND estado = 'APROBADA' AND fecha_hora_ingreso IS NULL
                ORDER BY fecha_hora_programada DESC NULLS LAST, creado_en DESC
                LIMIT 1
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, personaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVisita(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita aprobada pendiente", e);
        }
    }

    @Override
    public Optional<Visita> buscarVisitaActivaPorPersona(Long personaId) {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo
                FROM visitas
                WHERE persona_id = ? AND estado = 'DENTRO'
                ORDER BY fecha_hora_ingreso DESC NULLS LAST, creado_en DESC
                LIMIT 1
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, personaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVisita(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita activa por persona", e);
        }
    }

    private Visita mapVisita(ResultSet rs) throws SQLException {
        Visita visita = new Visita();
        visita.setId(rs.getLong("id"));
        visita.setPersonaId(rs.getLong("persona_id"));

        Long guardaId = rs.getLong("guarda_id");
        if (!rs.wasNull()) visita.setGuardaId(guardaId);

        Long funcionarioId = rs.getLong("funcionario_id");
        if (!rs.wasNull()) visita.setFuncionarioId(funcionarioId);

        Long empresaId = rs.getLong("empresa_visitada_id");
        if (!rs.wasNull()) visita.setEmpresaVisitadaId(empresaId);

        Timestamp prog = rs.getTimestamp("fecha_hora_programada");
        if (prog != null) visita.setFechaHoraProgramada(prog.toLocalDateTime());

        Timestamp ing = rs.getTimestamp("fecha_hora_ingreso");
        if (ing != null) visita.setFechaHoraIngreso(ing.toLocalDateTime());

        Timestamp sal = rs.getTimestamp("fecha_hora_salida");
        if (sal != null) visita.setFechaHoraSalida(sal.toLocalDateTime());

        visita.setEstado(EstadoVisita.valueOf(rs.getString("estado")));

        String motivo = rs.getString("motivo");
        if (!rs.wasNull()) visita.setMotivo(motivo);

        return visita;
    }

    private void setNullableLong(PreparedStatement stmt, int idx, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(idx, value);
        } else {
            stmt.setNull(idx, java.sql.Types.BIGINT);
        }
    }

    private void setNullableTimestamp(PreparedStatement stmt, int idx, LocalDateTime value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(idx, Timestamp.valueOf(value));
        } else {
            stmt.setNull(idx, java.sql.Types.TIMESTAMP);
        }
    }

    private void setNullableString(PreparedStatement stmt, int idx, String value) throws SQLException {
        if (value != null) {
            stmt.setString(idx, value);
        } else {
            stmt.setNull(idx, java.sql.Types.VARCHAR);
        }
    }

    @Override
    public Optional<Visita> buscarPorId(Long id) {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo
                FROM visitas
                WHERE id = ?
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVisita(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita por id", e);
        }
    }

    @Override
    public List<Visita> listarPendientesPorFuncionario(Long funcionarioId) {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo, creado_en
                FROM visitas
                WHERE funcionario_id = ?
                  AND estado IN ('PENDIENTE_APROBACION', 'PENDIENTE_APROBACION_OLVIDO')
                ORDER BY creado_en DESC
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, funcionarioId);

            List<Visita> resultado = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapVisita(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pendientes por funcionario", e);
        }
    }

    @Override
    public List<Visita> listarVisitasDentro() {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo
                FROM visitas
                WHERE estado = 'DENTRO'
                ORDER BY fecha_hora_ingreso DESC
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            List<Visita> resultado = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapVisita(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar visitas dentro", e);
        }
    }

    @Override
    public List<Visita> listarPorGuarda(Long guardaId) {
        String sql = """
                SELECT id, persona_id, guarda_id, funcionario_id, empresa_visitada_id,
                       fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida,
                       estado, motivo
                FROM visitas
                WHERE guarda_id = ?
                ORDER BY creado_en DESC
                LIMIT 20
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, guardaId);

            List<Visita> resultado = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapVisita(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar visitas por guarda", e);
        }
    }
}