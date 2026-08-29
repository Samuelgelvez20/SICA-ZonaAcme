package com.acme.sica.incidentes.infrastructure;

import com.acme.sica.config.ConexionPostgres;
import com.acme.sica.incidentes.application.IncidenteRepository;
import com.acme.sica.incidentes.domain.Incidente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IncidenteRepositoryJdbc implements IncidenteRepository {

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = """
                INSERT INTO incidentes (persona_id, usuario_id, tipo, descripcion, fecha_hora)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (incidente.getPersonaId() != null) {
                stmt.setLong(1, incidente.getPersonaId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setLong(2, incidente.getUsuarioId());
            stmt.setString(3, incidente.getTipo());
            stmt.setString(4, incidente.getDescripcion());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(incidente.getFechaHora()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    incidente.setId(rs.getLong("id"));
                }
            }
            return incidente;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar incidente", e);
        }
    }
}
