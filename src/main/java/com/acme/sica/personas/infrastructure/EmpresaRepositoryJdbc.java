package com.acme.sica.personas.infrastructure;

import com.acme.sica.config.ConexionPostgres;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.domain.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpresaRepositoryJdbc implements EmpresaRepository {

    @Override
    public Empresa guardar(Empresa empresa) {
        String sql = """
                INSERT INTO empresas (nombre, nit)
                VALUES (?, ?)
                RETURNING id
                """;
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empresa.getNombre());
            stmt.setString(2, empresa.getNit());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    empresa.setId(rs.getLong("id"));
                }
            }
            return empresa;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar empresa", e);
        }
    }

    @Override
    public Optional<Empresa> buscarPorNit(String nit) {
        String sql = "SELECT id, nombre, nit FROM empresas WHERE nit = ?";
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nit);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmpresa(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empresa por nit", e);
        }
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        String sql = "SELECT id, nombre, nit FROM empresas WHERE id = ?";
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmpresa(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empresa por id", e);
        }
    }

    @Override
    public List<Empresa> listarTodas() {
        String sql = "SELECT id, nombre, nit FROM empresas ORDER BY nombre";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = ConexionPostgres.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                empresas.add(mapEmpresa(rs));
            }
            return empresas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar empresas", e);
        }
    }

    private Empresa mapEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        empresa.setId(rs.getLong("id"));
        empresa.setNombre(rs.getString("nombre"));
        empresa.setNit(rs.getString("nit"));
        return empresa;
    }
}