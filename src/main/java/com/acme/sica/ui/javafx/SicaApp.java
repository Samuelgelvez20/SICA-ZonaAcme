package com.acme.sica.ui.javafx;

import com.acme.sica.usuarios.domain.Usuario;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Aplicación principal de JavaFX para SICA (Zona Acme).
 */
public class SicaApp extends Application {

    private Stage primaryStage;
    private ServiceRegistry services;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.services = ServiceRegistry.getInstance();

        primaryStage.setTitle("SICA - Sistema Integrado de Control de Acceso (Zona Acme)");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);

        if (getParameters().getUnnamed().contains("--dashboard")) {
            Usuario admin = services.getUsuarioRepository().buscarPorUsername("admin").orElse(null);
            if (admin != null) {
                showDashboard(admin);
                primaryStage.show();
                return;
            }
        }

        showLogin();
        primaryStage.show();
    }

    public void showLogin() {
        JavaFxLoginView loginView = new JavaFxLoginView(
                services.getAutenticarUsuarioService(),
                this::showDashboard
        );

        Scene scene = new Scene(loginView.getView(), 1050, 700);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void showDashboard(Usuario usuario) {
        JavaFxDashboardView dashboardView = new JavaFxDashboardView(
                usuario,
                services,
                this::showLogin
        );

        Scene scene = new Scene(dashboardView.getView(), 1200, 780);
        applyTheme(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void applyTheme(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/sica-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

