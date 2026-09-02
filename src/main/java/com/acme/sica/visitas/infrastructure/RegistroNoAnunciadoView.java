package com.acme.sica.visitas.infrastructure;

import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.application.RegistrarVisitaNoAnunciadaService;
import com.acme.sica.visitas.domain.Visita;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Vista para registrar un ingreso no anunciado.
 *
 * <p>Flujo en dos pasos:
 * 1. Buscar persona por documento
 * 2. Si existe: confirmar con funcionarioId y empresaVisitadaId
 *    Si no existe: mostrar formulario completo para crear persona y visita
 */
public class RegistroNoAnunciadoView extends JPanel {

    private final Usuario guardaActual;
    private final RegistrarVisitaNoAnunciadaService registrarService;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;

    // Paso 1: búsqueda
    private final JTextField txtDocumento;
    private final JButton btnBuscar;

    // Paso 2: datos de la persona (existente o nueva)
    private final JPanel panelDatosPersona;
    private final JLabel lblNombre;
    private final JLabel lblTipo;
    private final JLabel lblEmpresa;
    private final JTextField txtNombre;
    private final JComboBox<TipoPersona> cmbTipo;
    private final JTextField txtFotoUrl;
    private final JTextField txtEmpresaId;
    private final JComboBox<Usuario> cmbFuncionario;
    private final JTextField txtEmpresaVisitadaId;
    private final JButton btnRegistrar;
    private final JLabel lblStatus;

    private Optional<Persona> personaEncontrada = Optional.empty();
    private boolean esPersonaNueva = false;
    private boolean panelInicializado = false;

    public RegistroNoAnunciadoView(Usuario guardaActual,
                                    RegistrarVisitaNoAnunciadaService registrarService,
                                    PersonaRepository personaRepository,
                                    UsuarioRepository usuarioRepository) {
        this.guardaActual = guardaActual;
        this.registrarService = registrarService;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Ingreso No Anunciado - " + guardaActual.getNombre(),
                TitledBorder.LEFT, TitledBorder.TOP));

        // Panel superior: búsqueda por documento
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Paso 1: Buscar Persona"));
        panelBusqueda.add(new JLabel("Documento:"));
        txtDocumento = new JTextField(15);
        panelBusqueda.add(txtDocumento);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(this::onBuscar);
        panelBusqueda.add(btnBuscar);
        add(panelBusqueda, BorderLayout.NORTH);

        // Panel central: datos de la persona (inicialmente oculto)
        panelDatosPersona = new JPanel(new GridLayout(7, 2, 5, 5));
        panelDatosPersona.setBorder(BorderFactory.createTitledBorder("Paso 2: Datos de la Visita"));
        panelDatosPersona.setVisible(false);

        lblNombre = new JLabel("Nombre: ");
        lblTipo = new JLabel("Tipo: ");
        lblEmpresa = new JLabel("Empresa: ");

        panelDatosPersona.add(new JLabel("Nombre:"));
        panelDatosPersona.add(lblNombre);
        panelDatosPersona.add(new JLabel("Tipo:"));
        panelDatosPersona.add(lblTipo);
        panelDatosPersona.add(new JLabel("Empresa:"));
        panelDatosPersona.add(lblEmpresa);

        panelDatosPersona.add(new JLabel("Foto URL (opcional):"));
        txtFotoUrl = new JTextField();
        panelDatosPersona.add(txtFotoUrl);

        panelDatosPersona.add(new JLabel("Empresa ID (opcional):"));
        txtEmpresaId = new JTextField();
        panelDatosPersona.add(txtEmpresaId);

        panelDatosPersona.add(new JLabel("Funcionario Anfitrión (obligatorio):"));
        cmbFuncionario = new JComboBox<>();
        cargarFuncionarios();
        panelDatosPersona.add(cmbFuncionario);

        panelDatosPersona.add(new JLabel("Empresa Visitada ID (obligatorio):"));
        txtEmpresaVisitadaId = new JTextField();
        panelDatosPersona.add(txtEmpresaVisitadaId);

        // Campos editables solo para persona nueva (inicialmente no añadidos al panel)
        txtNombre = new JTextField();
        cmbTipo = new JComboBox<>(TipoPersona.values());
        cmbTipo.setSelectedItem(TipoPersona.INVITADO);

        add(panelDatosPersona, BorderLayout.CENTER);

        // Panel inferior: botón registrar y status
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        btnRegistrar = new JButton("Registrar Ingreso No Anunciado");
        btnRegistrar.addActionListener(this::onRegistrar);
        btnRegistrar.setEnabled(false);
        panelInferior.add(btnRegistrar, BorderLayout.NORTH);

        lblStatus = new JLabel("Ingrese documento y pulse Buscar");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelInferior.add(lblStatus, BorderLayout.CENTER);

        add(panelInferior, BorderLayout.SOUTH);

        // Enter en campo documento dispara búsqueda
        txtDocumento.addActionListener(this::onBuscar);
    }

    private void cargarFuncionarios() {
        cmbFuncionario.removeAllItems();
        for (Usuario u : usuarioRepository.listarTodos()) {
            if (u.isActivo() && "FUNCIONARIO".equals(u.getRol().getNombre())) {
                cmbFuncionario.addItem(u);
            }
        }
    }

    private void seleccionarFuncionarioPorPersona(Persona persona) {
        if (persona.getFuncionarioAnfitrionId() != null) {
            for (int i = 0; i < cmbFuncionario.getItemCount(); i++) {
                Usuario u = cmbFuncionario.getItemAt(i);
                if (u.getId().equals(persona.getFuncionarioAnfitrionId())) {
                    cmbFuncionario.setSelectedIndex(i);
                    return;
                }
            }
        }
        // Si no se encontró o es null, seleccionar el primero por defecto
        if (cmbFuncionario.getItemCount() > 0) {
            cmbFuncionario.setSelectedIndex(0);
        }
    }

    private void onBuscar(ActionEvent e) {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un documento para buscar.",
                    "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnBuscar.setEnabled(false);
        lblStatus.setText("Buscando persona...");

        new SwingWorker<Optional<Persona>, Void>() {
            @Override
            protected Optional<Persona> doInBackground() {
                return personaRepository.buscarPorDocumento(documento);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                try {
                    personaEncontrada = get();
                    if (personaEncontrada.isPresent()) {
                        // Persona existe
                        esPersonaNueva = false;
                        Persona p = personaEncontrada.get();
                        lblNombre.setText(p.getNombre());
                        lblTipo.setText(p.getTipo().name());
                        lblEmpresa.setText(p.getEmpresaId() != null ? p.getEmpresaId().toString() : "—");

                        // Asegurar que campos de creación no estén visibles
                        if (panelInicializado) {
                            panelDatosPersona.remove(txtNombre);
                            panelDatosPersona.remove(cmbTipo);
                        }

                        // Auto-seleccionar funcionario basado en la persona
                        seleccionarFuncionarioPorPersona(p);

                        lblStatus.setText("Persona encontrada. Complete funcionario y empresa visitada.");
                    } else {
                        // Persona NO existe - mostrar formulario de creación
                        esPersonaNueva = true;
                        lblNombre.setText("(nueva persona)");
                        lblTipo.setText("(nueva persona)");
                        lblEmpresa.setText("(nueva persona)");

                        // Mostrar campos de creación
                        reconstruirPanelParaNuevaPersona();

                        lblStatus.setText("Persona no existe. Complete los datos para crear nuevo ingreso.");
                    }

                    panelDatosPersona.setVisible(true);
                    btnRegistrar.setEnabled(true);
                    cmbFuncionario.requestFocusInWindow();
                    revalidate();
                    repaint();

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(RegistroNoAnunciadoView.this,
                            "Error al buscar: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void reconstruirPanelParaNuevaPersona() {
        panelDatosPersona.removeAll();
        panelDatosPersona.setLayout(new GridLayout(8, 2, 5, 5));
        panelDatosPersona.setBorder(BorderFactory.createTitledBorder("Paso 2: Datos de la Visita (Nueva Persona)"));

        panelDatosPersona.add(new JLabel("Nombre (obligatorio):"));
        panelDatosPersona.add(txtNombre);
        panelDatosPersona.add(new JLabel("Tipo:"));
        panelDatosPersona.add(cmbTipo);
        panelDatosPersona.add(new JLabel("Foto URL (opcional):"));
        panelDatosPersona.add(txtFotoUrl);
        panelDatosPersona.add(new JLabel("Empresa ID (opcional):"));
        panelDatosPersona.add(txtEmpresaId);
        panelDatosPersona.add(new JLabel("Funcionario Anfitrión (obligatorio):"));
        panelDatosPersona.add(cmbFuncionario);
        panelDatosPersona.add(new JLabel("Empresa Visitada ID (obligatorio):"));
        panelDatosPersona.add(txtEmpresaVisitadaId);
        panelDatosPersona.add(new JLabel(""));
        panelDatosPersona.add(new JLabel(""));

        panelInicializado = true;
    }

    private void onRegistrar(ActionEvent e) {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El documento es obligatorio",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario funcionarioSeleccionado = (Usuario) cmbFuncionario.getSelectedItem();
        if (funcionarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un funcionario anfitrión",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String empresaVisitadaIdStr = txtEmpresaVisitadaId.getText().trim();
        if (empresaVisitadaIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La empresa visitada es obligatoria",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnRegistrar.setEnabled(false);
        lblStatus.setText("Registrando ingreso...");

        Long funcionarioId = funcionarioSeleccionado.getId();
        Long empresaVisitadaId;
        try {
            empresaVisitadaId = Long.parseLong(empresaVisitadaIdStr);
        } catch (NumberFormatException ex) {
            btnRegistrar.setEnabled(true);
            lblStatus.setText("Error: ID de empresa inválido");
            JOptionPane.showMessageDialog(this, "El ID de empresa debe ser un número válido",
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombre = esPersonaNueva ? txtNombre.getText().trim() : null;
        TipoPersona tipo = esPersonaNueva ? (TipoPersona) cmbTipo.getSelectedItem() : null;
        String fotoUrl = esPersonaNueva ? txtFotoUrl.getText().trim() : null;
        String empresaIdStr = esPersonaNueva ? txtEmpresaId.getText().trim() : null;
        Long empresaId = (empresaIdStr != null && !empresaIdStr.isEmpty()) ? Long.parseLong(empresaIdStr) : null;

        if (esPersonaNueva && (nombre == null || nombre.isEmpty())) {
            btnRegistrar.setEnabled(true);
            lblStatus.setText("Error: nombre obligatorio para nueva persona");
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio para nueva persona",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<Visita, Void>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return registrarService.registrar(guardaActual, documento,
                        nombre, tipo, fotoUrl, empresaId, funcionarioId, empresaVisitadaId);
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    Visita visita = get();
                    lblStatus.setText("Solicitud enviada (ID: " + visita.getId() + "), esperando aprobación");
                    JOptionPane.showMessageDialog(
                            RegistroNoAnunciadoView.this,
                            "Ingreso no anunciado registrado correctamente.\n" +
                            "Estado: PENDIENTE_APROBACION\n" +
                            "El funcionario ha sido notificado.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE
                    );
                    // Limpiar formulario para siguiente ingreso
                    limpiarFormulario();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(
                            RegistroNoAnunciadoView.this,
                            "Error: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void limpiarFormulario() {
        txtDocumento.setText("");
        txtNombre.setText("");
        txtFotoUrl.setText("");
        txtEmpresaId.setText("");
        txtEmpresaVisitadaId.setText("");
        cmbTipo.setSelectedItem(TipoPersona.INVITADO);
        if (cmbFuncionario.getItemCount() > 0) {
            cmbFuncionario.setSelectedIndex(0);
        }

        personaEncontrada = Optional.empty();
        esPersonaNueva = false;
        panelDatosPersona.setVisible(false);
        btnRegistrar.setEnabled(false);
        lblStatus.setText("Ingrese documento y pulse Buscar");
        txtDocumento.requestFocusInWindow();

        // Restaurar panel original para búsqueda
        panelDatosPersona.removeAll();
        panelDatosPersona.setLayout(new GridLayout(7, 2, 5, 5));
        panelDatosPersona.setBorder(BorderFactory.createTitledBorder("Paso 2: Datos de la Visita"));

        lblNombre.setText("Nombre: ");
        lblTipo.setText("Tipo: ");
        lblEmpresa.setText("Empresa: ");

        panelDatosPersona.add(new JLabel("Nombre:"));
        panelDatosPersona.add(lblNombre);
        panelDatosPersona.add(new JLabel("Tipo:"));
        panelDatosPersona.add(lblTipo);
        panelDatosPersona.add(new JLabel("Empresa:"));
        panelDatosPersona.add(lblEmpresa);

        panelDatosPersona.add(new JLabel("Foto URL (opcional):"));
        panelDatosPersona.add(txtFotoUrl);

        panelDatosPersona.add(new JLabel("Empresa ID (opcional):"));
        panelDatosPersona.add(txtEmpresaId);

        panelDatosPersona.add(new JLabel("Funcionario Anfitrión (obligatorio):"));
        panelDatosPersona.add(cmbFuncionario);

        panelDatosPersona.add(new JLabel("Empresa Visitada ID (obligatorio):"));
        panelDatosPersona.add(txtEmpresaVisitadaId);

        panelInicializado = false;

        revalidate();
        repaint();
    }
}