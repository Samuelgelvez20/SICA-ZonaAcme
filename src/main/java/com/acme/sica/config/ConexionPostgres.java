package com.acme.sica.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionPostgres {

    private static final ConexionPostgres INSTANCE = new ConexionPostgres();

    private final String host;
    private final String port;
    private final String database;
    private final String user;
    private final String password;

    private ConexionPostgres() {
        this.host     = env("SICA_DB_HOST", "localhost");
        this.port     = env("SICA_DB_PORT", "5433");
        this.database = env("SICA_DB_NAME", "sica");
        this.user     = env("SICA_DB_USER", "sica_user");
        this.password = env("SICA_DB_PASSWORD", "sica_pass");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC de PostgreSQL no encontrado", e);
        }
    }

    public static ConexionPostgres getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
