package com.acme.sica.ui.javafx;

import com.acme.sica.shared.CredencialesInvalidasException;
import com.acme.sica.usuarios.application.AutenticarUsuarioService;
import com.acme.sica.usuarios.domain.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Pantalla de inicio de sesión moderna desarrollada en JavaFX.
 */
public class JavaFxLoginView {

    private final AutenticarUsuarioService autenticarUsuarioService;
    private final Consumer<Usuario> onLoginSuccess;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Label lblError;
    private Button btnLogin;

    public JavaFxLoginView(AutenticarUsuarioService autenticarUsuarioService, Consumer<Usuario> onLoginSuccess) {
        this.autenticarUsuarioService = autenticarUsuarioService;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Parent getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("app-container");
        root.setPadding(new Insets(40));

        // Contenedor tarjeta de login
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(36, 32, 36, 32));

        // Encabezado
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);

        Label badge = new Label("COMPLEJO ZONA ACME");
        badge.getStyleClass().addAll("badge", "badge-info");

        Label title = new Label("SICA");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #ffffff; -fx-letter-spacing: 2px;");

        Label subtitle = new Label("Sistema Integrado de Control de Acceso");
        subtitle.getStyleClass().add("card-subtitle");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        header.getChildren().addAll(badge, title, subtitle);

        // Mensaje de Error
        lblError = new Label();
        lblError.getStyleClass().addAll("badge", "badge-danger");
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setAlignment(Pos.CENTER);

        // Campos de entrada
        VBox form = new VBox(14);

        VBox userFieldBox = new VBox(6);
        Label lblUser = new Label("Usuario");
        lblUser.setStyle("-fx-font-weight: 600; -fx-text-fill: #cbd5e1;");
        txtUsername = new TextField();
        txtUsername.setPromptText("Ej. admin, guarda1, funcionario1");
        userFieldBox.getChildren().addAll(lblUser, txtUsername);

        VBox passFieldBox = new VBox(6);
        Label lblPass = new Label("Contraseña");
        lblPass.setStyle("-fx-font-weight: 600; -fx-text-fill: #cbd5e1;");
        txtPassword = new PasswordField();
        txtPassword.setPromptText("••••••••");
        passFieldBox.getChildren().addAll(lblPass, txtPassword);

        // Botón Iniciar Sesión
        btnLogin = new Button("Iniciar Sesión");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(42);
        btnLogin.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        btnLogin.setOnAction(e -> handleLogin());

        // Permitir enter para login
        txtUsername.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> handleLogin());

        form.getChildren().addAll(userFieldBox, passFieldBox, btnLogin);

        // Accesos rápidos para evaluación/testing
        VBox demoBox = new VBox(8);
        demoBox.setAlignment(Pos.CENTER);
        demoBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8px; -fx-padding: 12px;");

        Label lblDemo = new Label("Credenciales rápidas para prueba:");
        lblDemo.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox demoButtons = new HBox(8);
        demoButtons.setAlignment(Pos.CENTER);

        Button btnAdmin = createQuickButton("Admin", "admin", "1234");
        Button btnGuarda = createQuickButton("Guarda", "guarda1", "1234");
        Button btnFuncionario = createQuickButton("Funcionario", "funcionario1", "1234");

        demoButtons.getChildren().addAll(btnAdmin, btnGuarda, btnFuncionario);
        demoBox.getChildren().addAll(lblDemo, demoButtons);

        // Footer info
        Label footer = new Label("Desarrollado en Java 21 • JavaFX • PostgreSQL 16");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        VBox.setMargin(footer, new Insets(10, 0, 0, 0));
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(header, lblError, form, demoBox, footer);
        root.getChildren().add(card);

        return root;
    }

    private Button createQuickButton(String label, String user, String pass) {
        Button btn = new Button(label);
        btn.getStyleClass().add("btn-secondary");
        btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btn.setOnAction(e -> {
            txtUsername.setText(user);
            txtPassword.setText(pass);
            handleLogin();
        });
        return btn;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor completa el usuario y la contraseña.");
            return;
        }

        try {
            btnLogin.setDisable(true);
            Usuario usuario = autenticarUsuarioService.autenticar(username, password);
            lblError.setVisible(false);
            lblError.setManaged(false);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(usuario);
            }
        } catch (CredencialesInvalidasException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error de conexión: " + ex.getMessage());
        } finally {
            btnLogin.setDisable(false);
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
