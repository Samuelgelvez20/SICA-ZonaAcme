package com.acme.sica.auditoria.infrastructure;

import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.domain.BitacoraAuditoria;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.config.ConexionPostgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraRepositoryJdbc implements BitacoraRepository {

    @Override
    public void guardar(BitacoraAuditoria registro) {
        String sql = """
                INSERT INTO bitacora_auditoria (usuario_id, accion, entidad, detalle, fecha_hora, resultado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (registro.getUsuarioId() != null) {
                stmt.setLong(1, registro.getUsuarioId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, registro.getAccion());
            if (registro.getEntidad() != null) {
                stmt.setString(3, registro.getEntidad());
            } else {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            }
            stmt.setString(4, registro.getDetalle());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(registro.getFechaHora()));
            stmt.setString(6, registro.getResultado() == ResultadoAuditoria.EXITO ? "EXITO" : "FALLO");

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar en bitacora_auditoria", e);
        }
    }

    @Override
    public List<BitacoraAuditoria> listarConFiltros(Long usuarioId, String accion,
                                                     LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, usuario_id, accion, entidad, detalle, fecha_hora, resultado
                FROM bitacora_auditoria
                WHERE 1=1
                """);

        if (usuarioId != null) {
            sql.append(" AND usuario_id = ?");
        }
        if (accion != null && !accion.isBlank()) {
            sql.append(" AND accion = ?");
        }
        if (fechaDesde != null) {
            sql.append(" AND fecha_hora >= ?");
        }
        if (fechaHasta != null) {
            sql.append(" AND fecha_hora < ?");
        }
        sql.append(" ORDER BY fecha_hora DESC");

        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (usuarioId != null) {
                stmt.setLong(idx++, usuarioId);
            }
            if (accion != null && !accion.isBlank()) {
                stmt.setString(idx++, accion);
            }
            if (fechaDesde != null) {
                stmt.setTimestamp(idx++, java.sql.Timestamp.valueOf(fechaDesde));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(idx++, java.sql.Timestamp.valueOf(fechaHasta));
            }

            List<BitacoraAuditoria> registros = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapRegistro(rs));
                }
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar bitacora con filtros", e);
        }
    }

    private BitacoraAuditoria mapRegistro(ResultSet rs) throws SQLException {
        BitacoraAuditoria reg = new BitacoraAuditoria();
        reg.setId(rs.getLong("id"));
        Long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            reg.setUsuarioId(usuarioId);
        }
        reg.setAccion(rs.getString("accion"));
        reg.setEntidad(rs.getString("entidad"));
        reg.setDetalle(rs.getString("detalle"));
        reg.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        reg.setResultado("EXITO".equals(rs.getString("resultado"))
                ? ResultadoAuditoria.EXITO : ResultadoAuditoria.FALLO);
        return reg;
    }
}