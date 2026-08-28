package com.acme.sica.auditoria.infrastructure;

import com.acme.sica.auditoria.application.BitacoraRepository;
import com.acme.sica.auditoria.domain.BitacoraAuditoria;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;
import com.acme.sica.config.ConexionPostgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}