package com.acme.sica.ui.javafx;

import com.acme.sica.personas.domain.Empresa;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.reportes.application.FiltrosBitacora;
import com.acme.sica.reportes.application.ReporteBitacora;
import com.acme.sica.reportes.application.ReporteVisitasDentro;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.domain.Visita;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Panel de control principal moderno desarrollado en JavaFX.
 * Reemplaza los diálogos dispersos y JOptionPane por una experiencia de usuario
 * unificada con barra lateral, tarjetas visuales, tablas interactivas y retroalimentación fluida.
 */
public class JavaFxDashboardView {

    private final Usuario usuarioActual;
    private final ServiceRegistry services;
    private final Runnable onLogout;

    private BorderPane mainLayout;
    private StackPane contentArea;
    private Label lblSectionTitle;
    private Label lblSectionSubtitle;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public JavaFxDashboardView(Usuario usuarioActual, ServiceRegistry services, Runnable onLogout) {
        this.usuarioActual = usuarioActual;
        this.services = services;
        this.onLogout = onLogout;
    }

    public Parent getView() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("app-container");

        // 1. Sidebar lateral izquierda
        VBox sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        // 2. Contenedor central (TopBar + Content)
        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.TOP_LEFT);

        HBox topBar = buildTopBar();
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(24));
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        centerBox.getChildren().addAll(topBar, contentArea);
        mainLayout.setCenter(centerBox);

        // Cargar vista inicial según el rol
        String rol = usuarioActual.getRol().getNombre();
        if ("GUARDA".equals(rol)) {
            showCheckInView();
        } else if ("FUNCIONARIO".equals(rol)) {
            showPendientesView();
        } else {
            showPersonasDentroReport();
        }

        return mainLayout;
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        lblSectionTitle = new Label("Panel de Control");
        lblSectionTitle.getStyleClass().add("card-header");
        lblSectionSubtitle = new Label("Bienvenido al sistema de seguridad de Zona Acme");
        lblSectionSubtitle.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblSectionTitle, lblSectionSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status pill
        HBox dbStatus = new HBox(6);
        dbStatus.setAlignment(Pos.CENTER);
        dbStatus.getStyleClass().addAll("badge", "badge-success");
        Label dbDot = new Label("●");
        dbDot.setStyle("-fx-font-size: 10px; -fx-text-fill: #34d399;");
        Label dbText = new Label("PostgreSQL 16 Activo");
        dbText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
        dbStatus.getChildren().addAll(dbDot, dbText);

        topBar.getChildren().addAll(titleBox, spacer, dbStatus);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(20, 14, 20, 14));

        // Brand
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(4, 10, 16, 10));

        Label logoIcon = new Label("🏢");
        logoIcon.setStyle("-fx-font-size: 22px;");

        VBox brandText = new VBox(0);
        Label brandTitle = new Label("SICA");
        brandTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");
        Label brandDesc = new Label("Zona Acme Complejo");
        brandDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        brandText.getChildren().addAll(brandTitle, brandDesc);

        brand.getChildren().addAll(logoIcon, brandText);

        // Perfil usuario actual
        VBox userCard = new VBox(6);
        userCard.setStyle("-fx-background-color: #131c2e; -fx-background-radius: 10px; -fx-padding: 12px; -fx-border-color: #1e293b; -fx-border-radius: 10px;");

        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(usuarioActual.getNombre().substring(0, Math.min(2, usuarioActual.getNombre().length())).toUpperCase());
        avatar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9999px; -fx-padding: 6 10; -fx-font-size: 12px;");

        VBox userDetails = new VBox(2);
        Label userName = new Label(usuarioActual.getNombre());
        userName.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 13px;");
        Label userRole = new Label(usuarioActual.getRol().getNombre());
        userRole.getStyleClass().addAll("badge", "badge-info");
        userDetails.getChildren().addAll(userName, userRole);

        userInfo.getChildren().addAll(avatar, userDetails);
        userCard.getChildren().add(userInfo);

        // Menú de navegación
        ScrollPane scrollNav = new ScrollPane();
        scrollNav.setFitToWidth(true);
        scrollNav.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollNav, Priority.ALWAYS);

        VBox navList = new VBox(4);
        String rol = usuarioActual.getRol().getNombre();

        // Sección Portería
        if ("GUARDA".equals(rol) || "ADMIN".equals(rol)) {
            navList.getChildren().add(createNavHeader("PORTERÍA Y ACCESO"));
            navList.getChildren().add(createNavBtn("🚪 Check-in Invitado", e -> showCheckInView()));
            navList.getChildren().add(createNavBtn("🚶 Check-out (Salida)", e -> showCheckOutView()));
            navList.getChildren().add(createNavBtn("👷 Ingreso Trabajador", e -> showIngresoTrabajadorView()));
            navList.getChildren().add(createNavBtn("⚠️ Ingreso por Olvido", e -> showIngresoOlvidoView()));
            navList.getChildren().add(createNavBtn("📝 Visita No Anunciada", e -> showVisitaNoAnunciadaView()));
        }

        // Sección Funcionarios
        if ("FUNCIONARIO".equals(rol) || "ADMIN".equals(rol)) {
            navList.getChildren().add(createNavHeader("GESTIÓN DE VISITAS"));
            navList.getChildren().add(createNavBtn("📩 Solicitudes Pendientes", e -> showPendientesView()));
            navList.getChildren().add(createNavBtn("📅 Pre-registrar Invitado", e -> showPreRegistroView()));
            navList.getChildren().add(createNavBtn("🚨 Registrar Incidente", e -> showIncidenteView()));
        }

        // Sección Gestión & Administración
        navList.getChildren().add(createNavHeader("ADMINISTRACIÓN"));
        navList.getChildren().add(createNavBtn("👥 Personas y Carnets", e -> showPersonasView()));
        navList.getChildren().add(createNavBtn("🏢 Directorio de Empresas", e -> showEmpresasView()));

        // Sección Reportes
        if ("FUNCIONARIO".equals(rol) || "ADMIN".equals(rol)) {
            navList.getChildren().add(createNavHeader("INFORMES EN VIVO"));
            navList.getChildren().add(createNavBtn("📊 Personal Dentro del Complejo", e -> showPersonasDentroReport()));
            navList.getChildren().add(createNavBtn("📜 Bitácora de Auditoría", e -> showBitacoraReport()));
        }

        scrollNav.setContent(navList);

        // Botón Logout
        Button btnLogout = new Button("🚪 Cerrar Sesión");
        btnLogout.getStyleClass().add("btn-secondary");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        sidebar.getChildren().addAll(brand, userCard, scrollNav, btnLogout);
        return sidebar;
    }

    private Label createNavHeader(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-section-title");
        return lbl;
    }

    private Button createNavBtn(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        return btn;
    }

    private void updateHeader(String title, String subtitle) {
        lblSectionTitle.setText(title);
        lblSectionSubtitle.setText(subtitle);
    }

    // =========================================================================
    // VISTAS DEL SISTEMA
    // =========================================================================

    /**
     * Vista 1: Check-in de Invitado
     */
    private void showCheckInView() {
        updateHeader("Check-in de Invitado", "Verificación y autorización de ingreso para visitas aprobadas");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox searchCard = new VBox(14);
        searchCard.getStyleClass().add("card");

        Label cardTitle = new Label("Puesto de Control de Portería");
        cardTitle.getStyleClass().add("card-header");

        Label cardDesc = new Label("Ingresa el documento del visitante para verificar su visita aprobada y autorizar el acceso.");
        cardDesc.getStyleClass().add("card-subtitle");

        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Número de documento (ej. 1009876543)");
        txtDoc.setPrefHeight(40);
        HBox.setHgrow(txtDoc, Priority.ALWAYS);

        Button btnAction = new Button("Buscar y Autorizar");
        btnAction.getStyleClass().add("btn-primary");
        btnAction.setPrefHeight(40);

        inputBox.getChildren().addAll(txtDoc, btnAction);

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        VBox resultBox = new VBox(10);
        resultBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 16px; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-radius: 8px;");
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        Label resHeader = new Label("Detalles de la Visita Registrada");
        resHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label resContent = new Label();
        resContent.setStyle("-fx-font-family: monospace; -fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        resultBox.getChildren().addAll(resHeader, resContent);

        btnAction.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            if (doc.isEmpty()) {
                lblFeedback.setText("Por favor digita un número de documento.");
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                return;
            }

            try {
                Visita visita = services.getRegistrarCheckInService().ejecutar(usuarioActual, doc);
                lblFeedback.setText("✓ ¡Check-in exitoso! Ingreso concedido.");
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);

                String hora = visita.getFechaHoraIngreso() != null ? dtf.format(visita.getFechaHoraIngreso()) : "N/A";
                resContent.setText("ID Visita: " + visita.getId() + "\n"
                        + "Documento: " + doc + "\n"
                        + "Hora de Ingreso: " + hora + "\n"
                        + "Estado: " + visita.getEstado() + "\n"
                        + "Registrado por Guarda: " + usuarioActual.getNombre());
                resultBox.setVisible(true);
                resultBox.setManaged(true);
                txtDoc.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                resultBox.setVisible(false);
                resultBox.setManaged(false);
            }
        });

        searchCard.getChildren().addAll(cardTitle, cardDesc, inputBox, lblFeedback, resultBox);
        view.getChildren().add(searchCard);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 2: Check-out (Salida)
     */
    private void showCheckOutView() {
        updateHeader("Registro de Salida (Check-out)", "Cierre de visitas y salida física del complejo empresarial");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Control de Salida");
        cardTitle.getStyleClass().add("card-header");

        Label cardDesc = new Label("Digita el documento de la persona para registrar su hora de salida y cerrar la visita activa.");
        cardDesc.getStyleClass().add("card-subtitle");

        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Documento del visitante");
        txtDoc.setPrefHeight(40);
        HBox.setHgrow(txtDoc, Priority.ALWAYS);

        Button btnAction = new Button("Registrar Salida");
        btnAction.getStyleClass().add("btn-danger");
        btnAction.setPrefHeight(40);

        inputBox.getChildren().addAll(txtDoc, btnAction);

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnAction.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            if (doc.isEmpty()) {
                lblFeedback.setText("Ingresa el documento.");
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                return;
            }
            try {
                Visita v = services.getRegistrarCheckOutService().registrar(usuarioActual, doc);
                lblFeedback.setText("✓ Salida registrada con éxito. Visita ID #" + v.getId() + " FINALIZADA.");
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(cardTitle, cardDesc, inputBox, lblFeedback);
        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 3: Ingreso Trabajador
     */
    private void showIngresoTrabajadorView() {
        updateHeader("Ingreso de Trabajador", "Registro directo de colaboradores vinculados a empresas del complejo");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Control de Acceso de Trabajadores");
        cardTitle.getStyleClass().add("card-header");

        Label cardDesc = new Label("Verifica el carnet o documento de identidad del trabajador de planta o contratista.");
        cardDesc.getStyleClass().add("card-subtitle");

        HBox inputBox = new HBox(12);
        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Documento del trabajador");
        txtDoc.setPrefHeight(40);
        HBox.setHgrow(txtDoc, Priority.ALWAYS);

        Button btnAction = new Button("Permitir Ingreso");
        btnAction.getStyleClass().add("btn-success");
        btnAction.setPrefHeight(40);

        inputBox.getChildren().addAll(txtDoc, btnAction);

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnAction.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            if (doc.isEmpty()) return;
            try {
                Visita v = services.getRegistrarIngresoTrabajadorService().registrar(usuarioActual, doc);
                lblFeedback.setText("✓ Ingreso autorizado para el trabajador. Visita #" + v.getId());
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(cardTitle, cardDesc, inputBox, lblFeedback);
        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 4: Ingreso por Olvido de Carnet
     */
    private void showIngresoOlvidoView() {
        updateHeader("Ingreso por Olvido de Carnet", "Protocolo de contingencia para colaboradores sin identificación física");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("Registro de Ingreso por Olvido");
        cardTitle.getStyleClass().add("card-header");

        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Número de documento");

        Button btnSubmit = new Button("Validar y Autorizar Ingreso");
        btnSubmit.getStyleClass().add("btn-primary");

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnSubmit.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            if (doc.isEmpty()) return;
            try {
                Visita v = services.getRegistrarIngresoPorOlvidoService().registrar(usuarioActual, doc);
                lblFeedback.setText("✓ Ingreso por olvido autorizado (Visita #" + v.getId() + ")");
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(cardTitle, new Label("Documento:"), txtDoc, btnSubmit, lblFeedback);
        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 5: Visita No Anunciada
     */
    private void showVisitaNoAnunciadaView() {
        updateHeader("Visita No Anunciada", "Registro inmediato en portería con solicitud de aprobación al anfitrión");

        VBox view = new VBox(20);
        view.setMaxWidth(750);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        TextField txtDoc = new TextField();
        TextField txtNombre = new TextField();

        ComboBox<Empresa> cbEmpresas = new ComboBox<>();
        try {
            cbEmpresas.setItems(FXCollections.observableArrayList(services.getEmpresaRepository().listarTodas()));
        } catch (Exception ignored) {}

        ComboBox<Usuario> cbAnfitriones = new ComboBox<>();
        try {
            List<Usuario> funcs = services.getUsuarioRepository().listarTodos().stream()
                    .filter(u -> "FUNCIONARIO".equals(u.getRol().getNombre()) || "ADMIN".equals(u.getRol().getNombre()))
                    .toList();
            cbAnfitriones.setItems(FXCollections.observableArrayList(funcs));
        } catch (Exception ignored) {}

        grid.add(new Label("Documento:"), 0, 0);
        grid.add(txtDoc, 1, 0);
        grid.add(new Label("Nombre Completo:"), 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(new Label("Empresa Destino:"), 0, 2);
        grid.add(cbEmpresas, 1, 2);
        grid.add(new Label("Funcionario Anfitrión:"), 0, 3);
        grid.add(cbAnfitriones, 1, 3);

        Button btnRegistrar = new Button("Crear Solicitud y Notificar");
        btnRegistrar.getStyleClass().add("btn-primary");

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnRegistrar.setOnAction(e -> {
            Empresa emp = cbEmpresas.getValue();
            Usuario anf = cbAnfitriones.getValue();
            if (emp == null || anf == null || txtDoc.getText().isEmpty() || txtNombre.getText().isEmpty()) {
                lblFeedback.setText("Por favor diligencia todos los campos requeridos.");
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                return;
            }

            try {
                Visita v = services.getRegistrarVisitaNoAnunciadaService().registrar(
                        usuarioActual,
                        txtDoc.getText().trim(),
                        txtNombre.getText().trim(),
                        TipoPersona.INVITADO,
                        null,
                        emp.getId(),
                        anf.getId(),
                        emp.getId()
                );
                lblFeedback.setText("✓ Solicitud creada con éxito (Visita #" + v.getId() + "). Esperando aprobación.");
                lblFeedback.getStyleClass().setAll("badge", "badge-warning");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
                txtNombre.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(grid, btnRegistrar, lblFeedback);
        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 6: Solicitudes Pendientes (para Funcionarios/Admin)
     */
    private void showPendientesView() {
        updateHeader("Solicitudes Pendientes de Aprobación", "Bandeja de visitas que requieren autorización para ingreso");

        VBox view = new VBox(16);

        HBox actionsBar = new HBox(12);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        Button btnAprobar = new Button("✓ Aprobar Seleccionada");
        btnAprobar.getStyleClass().add("btn-success");

        Button btnRechazar = new Button("✕ Rechazar Seleccionada");
        btnRechazar.getStyleClass().add("btn-danger");

        Button btnRefresh = new Button("🔄 Actualizar");
        btnRefresh.getStyleClass().add("btn-secondary");

        actionsBar.getChildren().addAll(btnAprobar, btnRechazar, btnRefresh);

        TableView<Visita> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Visita, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setMaxWidth(60);

        TableColumn<Visita, String> colPersona = new TableColumn<>("Visitante");
        colPersona.setCellValueFactory(d -> {
            var pOpt = services.getPersonaRepository().buscarPorId(d.getValue().getPersonaId());
            return new SimpleStringProperty(pOpt.map(p -> p.getNombre() + " (" + p.getDocumento() + ")").orElse("N/A"));
        });

        TableColumn<Visita, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo() != null ? d.getValue().getMotivo() : "Sin especificar"));

        TableColumn<Visita, String> colFecha = new TableColumn<>("Programada / Solicitada");
        colFecha.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getFechaHoraProgramada();
            return new SimpleStringProperty(dt != null ? dtf.format(dt) : "Ingreso Inmediato");
        });

        TableColumn<Visita, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        table.getColumns().addAll(colId, colPersona, colMotivo, colFecha, colEstado);

        Runnable loadData = () -> {
            try {
                List<Visita> list = services.getVisitaRepository().listarPendientesPorFuncionario(usuarioActual.getId());
                table.setItems(FXCollections.observableArrayList(list));
            } catch (Exception ex) {
                try {
                    table.setItems(FXCollections.observableArrayList());
                } catch (Exception ignored) {}
            }
        };

        btnRefresh.setOnAction(e -> loadData.run());

        btnAprobar.setOnAction(e -> {
            Visita sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                services.getAprobarORechazarService().aprobar(usuarioActual, sel.getId());
                loadData.run();
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        btnRechazar.setOnAction(e -> {
            Visita sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            TextInputDialog dialog = new TextInputDialog("No autorizado");
            dialog.setTitle("Rechazar Visita");
            dialog.setHeaderText("Indica el motivo del rechazo para la visita #" + sel.getId());
            dialog.setContentText("Motivo:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(motivo -> {
                try {
                    services.getAprobarORechazarService().rechazar(usuarioActual, sel.getId(), motivo);
                    loadData.run();
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });

        loadData.run();
        view.getChildren().addAll(actionsBar, table);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 7: Pre-registro de Invitado
     */
    private void showPreRegistroView() {
        updateHeader("Pre-registrar Invitado", "Programación de visitas oficiales con antelación");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Documento del invitado");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");

        DatePicker dpFecha = new DatePicker();
        dpFecha.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Empresa> cbEmpresas = new ComboBox<>();
        try {
            cbEmpresas.setItems(FXCollections.observableArrayList(services.getEmpresaRepository().listarTodas()));
        } catch (Exception ignored) {}

        Button btnGuardar = new Button("Guardar y Pre-autorizar");
        btnGuardar.getStyleClass().add("btn-primary");

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnGuardar.setOnAction(e -> {
            if (txtDoc.getText().isEmpty() || txtNombre.getText().isEmpty() || dpFecha.getValue() == null || cbEmpresas.getValue() == null) {
                lblFeedback.setText("Todos los campos son obligatorios.");
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                return;
            }

            try {
                String doc = txtDoc.getText().trim();
                String nombre = txtNombre.getText().trim();
                Empresa emp = cbEmpresas.getValue();

                // Buscar o registrar persona
                Persona persona = services.getPersonaRepository().buscarPorDocumento(doc).orElseGet(() ->
                    services.getCrearPersonaService().ejecutar(usuarioActual, nombre, doc, TipoPersona.INVITADO, null, emp.getId(), null)
                );

                LocalDateTime fProg = dpFecha.getValue().atTime(9, 0);
                Visita v = services.getPreRegistrarInvitadoService().ejecutar(
                        usuarioActual,
                        persona.getId(),
                        emp.getId(),
                        fProg
                );
                lblFeedback.setText("✓ Pre-registro exitoso. ID Visita #" + v.getId() + " - Estado: APROBADA");
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
                txtNombre.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(
                new Label("Documento:"), txtDoc,
                new Label("Nombre completo:"), txtNombre,
                new Label("Empresa a visitar:"), cbEmpresas,
                new Label("Fecha programada:"), dpFecha,
                btnGuardar, lblFeedback
        );

        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 8: Reporte Personas Dentro del Complejo (Tiempo Real)
     */
    private void showPersonasDentroReport() {
        updateHeader("Personal en el Complejo", "Monitoreo en tiempo real de todas las personas presentes físicamente");

        VBox view = new VBox(16);

        // Tarjetas de estadísticas
        HBox statsRow = new HBox(16);

        VBox statTotal = new VBox(4);
        statTotal.getStyleClass().add("stat-card");
        statTotal.setPrefWidth(220);
        Label lblCount = new Label("0");
        lblCount.getStyleClass().add("stat-value");
        Label lblCountDesc = new Label("PERSONAS DENTRO AHORA");
        lblCountDesc.getStyleClass().add("stat-label");
        statTotal.getChildren().addAll(lblCount, lblCountDesc);

        statsRow.getChildren().add(statTotal);

        // Tabla
        TableView<ReporteVisitasDentro> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ReporteVisitasDentro, String> colPersona = new TableColumn<>("Nombre Persona");
        colPersona.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombrePersona()));

        TableColumn<ReporteVisitasDentro, String> colDoc = new TableColumn<>("Documento");
        colDoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumentoPersona()));

        TableColumn<ReporteVisitasDentro, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoPersona()));

        TableColumn<ReporteVisitasDentro, String> colEmpresa = new TableColumn<>("Empresa");
        colEmpresa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreEmpresa() != null ? d.getValue().getNombreEmpresa() : "N/A"));

        TableColumn<ReporteVisitasDentro, String> colIngreso = new TableColumn<>("Hora Ingreso");
        colIngreso.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaHoraIngreso() != null ? dtf.format(d.getValue().getFechaHoraIngreso()) : "N/A"));

        TableColumn<ReporteVisitasDentro, String> colGuarda = new TableColumn<>("Guarda de Entrada");
        colGuarda.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreGuarda() != null ? d.getValue().getNombreGuarda() : "N/A"));

        table.getColumns().addAll(colPersona, colDoc, colTipo, colEmpresa, colIngreso, colGuarda);

        try {
            List<ReporteVisitasDentro> items = services.getGenerarReporteVisitasDentroService().ejecutar(usuarioActual);
            table.setItems(FXCollections.observableArrayList(items));
            lblCount.setText(String.valueOf(items.size()));
        } catch (Exception ex) {
            showAlert("Error al generar reporte", ex.getMessage(), Alert.AlertType.ERROR);
        }

        view.getChildren().addAll(statsRow, table);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 9: Bitácora de Auditoría
     */
    private void showBitacoraReport() {
        updateHeader("Bitácora de Auditoría", "Trazabilidad inmutable de todas las operaciones realizadas en SICA");

        VBox view = new VBox(16);

        HBox filtersBar = new HBox(12);
        filtersBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtAccion = new TextField();
        txtAccion.setPromptText("Filtrar por acción (ej. CHECK_IN, LOGIN)");

        Button btnFiltrar = new Button("🔍 Filtrar");
        btnFiltrar.getStyleClass().add("btn-primary");

        filtersBar.getChildren().addAll(new Label("Acción:"), txtAccion, btnFiltrar);

        TableView<ReporteBitacora> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ReporteBitacora, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setMaxWidth(60);

        TableColumn<ReporteBitacora, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreUsuario()));

        TableColumn<ReporteBitacora, String> colAccion = new TableColumn<>("Acción");
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccion()));

        TableColumn<ReporteBitacora, String> colEntidad = new TableColumn<>("Entidad");
        colEntidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntidad()));

        TableColumn<ReporteBitacora, String> colDetalle = new TableColumn<>("Detalle");
        colDetalle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDetalle()));

        TableColumn<ReporteBitacora, String> colFecha = new TableColumn<>("Fecha y Hora");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaHora() != null ? dtf.format(d.getValue().getFechaHora()) : "N/A"));

        TableColumn<ReporteBitacora, String> colResultado = new TableColumn<>("Resultado");
        colResultado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResultado() != null ? d.getValue().getResultado().name() : "N/A"));

        table.getColumns().addAll(colId, colUsuario, colAccion, colEntidad, colDetalle, colFecha, colResultado);

        Runnable queryData = () -> {
            try {
                FiltrosBitacora fb = new FiltrosBitacora(null, null, null, txtAccion.getText().trim().isEmpty() ? null : txtAccion.getText().trim());
                List<ReporteBitacora> items = services.getGenerarReporteBitacoraService().ejecutar(usuarioActual, fb);
                table.setItems(FXCollections.observableArrayList(items));
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        };

        btnFiltrar.setOnAction(e -> queryData.run());
        queryData.run();

        view.getChildren().addAll(filtersBar, table);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 10: Directorio de Personas
     */
    private void showPersonasView() {
        updateHeader("Gestión de Personas", "Directorio general de trabajadores, invitados y contratistas");

        VBox view = new VBox(16);

        TableView<Persona> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Persona, String> colDoc = new TableColumn<>("Documento");
        colDoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumento()));

        TableColumn<Persona, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<Persona, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));

        TableColumn<Persona, String> colBloqueado = new TableColumn<>("Bloqueo de Acceso");
        colBloqueado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isBloqueado() ? "🔴 BLOQUEADO: " + d.getValue().getMotivoBloqueo() : "🟢 PERMITIDO"));

        table.getColumns().addAll(colDoc, colNombre, colTipo, colBloqueado);

        try {
            table.setItems(FXCollections.observableArrayList(services.getPersonaRepository().listarTodas()));
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }

        view.getChildren().add(table);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 11: Directorio de Empresas
     */
    private void showEmpresasView() {
        updateHeader("Directorio de Empresas", "Compañías radicadas en el complejo Zona Acme");

        VBox view = new VBox(16);

        TableView<Empresa> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Empresa, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setMaxWidth(60);

        TableColumn<Empresa, String> colNombre = new TableColumn<>("Razón Social");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<Empresa, String> colNit = new TableColumn<>("NIT");
        colNit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNit()));

        table.getColumns().addAll(colId, colNombre, colNit);

        try {
            table.setItems(FXCollections.observableArrayList(services.getEmpresaRepository().listarTodas()));
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
        }

        view.getChildren().add(table);
        contentArea.getChildren().setAll(view);
    }

    /**
     * Vista 12: Registro de Incidentes
     */
    private void showIncidenteView() {
        updateHeader("Registrar Incidente", "Reporte de anomalías y bloqueo preventivo de seguridad");

        VBox view = new VBox(20);
        view.setMaxWidth(700);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Documento de la persona involucrada");

        TextField txtTipo = new TextField();
        txtTipo.setPromptText("Tipo de incidente (ej. Conducta inapropiada, Acceso no autorizado)");

        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descripción detallada del incidente de seguridad...");
        txtDesc.setPrefRowCount(3);

        CheckBox chkBloquear = new CheckBox("Aplicar Bloqueo Inmediato de Acceso al Complejo");
        chkBloquear.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");

        Button btnReportar = new Button("Registrar Incidente");
        btnReportar.getStyleClass().add("btn-danger");

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setManaged(false);

        btnReportar.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            String tipo = txtTipo.getText().trim();
            String desc = txtDesc.getText().trim();
            if (doc.isEmpty() || desc.isEmpty()) return;

            try {
                var personaOpt = services.getPersonaRepository().buscarPorDocumento(doc);
                if (personaOpt.isEmpty()) {
                    lblFeedback.setText("No se encontró ninguna persona con el documento " + doc);
                    lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                    lblFeedback.setVisible(true);
                    lblFeedback.setManaged(true);
                    return;
                }

                Persona persona = personaOpt.get();
                services.getRegistrarIncidenteService().ejecutar(
                        usuarioActual,
                        persona.getId(),
                        tipo.isEmpty() ? "INCIDENTE" : tipo,
                        desc
                );

                if (chkBloquear.isSelected()) {
                    services.getBloquearPersonaService().ejecutar(usuarioActual, persona.getId(), desc);
                }

                lblFeedback.setText("✓ Incidente reportado exitosamente." + (chkBloquear.isSelected() ? " Persona bloqueada." : ""));
                lblFeedback.getStyleClass().setAll("badge", "badge-success");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
                txtDoc.clear();
                txtTipo.clear();
                txtDesc.clear();
            } catch (Exception ex) {
                lblFeedback.setText("❌ " + ex.getMessage());
                lblFeedback.getStyleClass().setAll("badge", "badge-danger");
                lblFeedback.setVisible(true);
                lblFeedback.setManaged(true);
            }
        });

        card.getChildren().addAll(
                new Label("Documento Persona:"), txtDoc,
                new Label("Tipo de Incidente:"), txtTipo,
                new Label("Descripción del Incidente:"), txtDesc,
                chkBloquear,
                btnReportar,
                lblFeedback
        );

        view.getChildren().add(card);
        contentArea.getChildren().setAll(view);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
