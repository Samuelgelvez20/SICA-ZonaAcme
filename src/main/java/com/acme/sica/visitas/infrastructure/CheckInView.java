package com.acme.sica.visitas.infrastructure;

import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.domain.Visita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Pantalla de Check-in para el Guarda.
 *
 * <p>Decisión de UX: flujo en un solo paso ("Buscar y Check-in").
 * Al hacer clic en el botón, se busca la persona por documento y, si tiene
 * una visita aprobada pendiente, se registra el check-in automáticamente.
 * Esto simplifica la operación del Guarda en el puesto de control.
 * La alternativa de dos pasos (buscar → confirmar) se deja para HU-16.
 */
public class CheckInView extends JFrame {

    private final RegistrarCheckInService checkInService;
    private final Usuario usuarioActual;

    private JTextField txtDocumento;
    private JButton btnCheckIn;
    private JTextArea txtResultado;

    public CheckInView(RegistrarCheckInService checkInService, Usuario usuarioActual) {
        this.checkInService = checkInService;
        this.usuarioActual = usuarioActual;
        initComponents();
    }

    private void initComponents() {
        setTitle("SICA - Check-in de Invitado (Guarda: " + usuarioActual.getNombre() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior: campo de documento + botón
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Datos del Visitante"));

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelCampos.add(new JLabel("Documento:"));
        txtDocumento = new JTextField(20);
        panelCampos.add(txtDocumento);

        btnCheckIn = new JButton("Buscar y Check-in");
        btnCheckIn.addActionListener(this::onCheckIn);
        panelCampos.add(btnCheckIn);

        panelSuperior.add(panelCampos, BorderLayout.NORTH);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central: resultado
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        JScrollPane scroll = new JScrollPane(txtResultado);
        add(scroll, BorderLayout.CENTER);

        // Panel inferior: info del usuario
        JLabel lblInfo = new JLabel("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getUsername() + ")");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(lblInfo, BorderLayout.SOUTH);

        // Enter en el campo dispara el check-in
        txtDocumento.addActionListener(this::onCheckIn);
    }

    private void onCheckIn(ActionEvent e) {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un documento para buscar.",
                    "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnCheckIn.setEnabled(false);
        txtResultado.setText("Buscando...");

        // Ejecutar en hilo de fondo para no bloquear la UI
        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() {
                return checkInService.ejecutar(usuarioActual, documento);
            }

            @Override
            protected void done() {
                btnCheckIn.setEnabled(true);
                try {
                    Visita visita = get();
                    mostrarExito(visita);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarError(cause);
                }
            }
        };
        worker.execute();
    }

    private void mostrarExito(Visita visita) {
        // Buscar datos de la persona y empresa para mostrar
        StringBuilder sb = new StringBuilder();
        sb.append("✓ CHECK-IN EXITOSO\n\n");
        sb.append("Persona: ").append(visita.getPersonaId()).append("\n");
        sb.append("Empresa visitada ID: ").append(visita.getEmpresaVisitadaId()).append("\n");
        sb.append("Fecha/hora ingreso: ").append(visita.getFechaHoraIngreso()).append("\n");
        sb.append("Estado: ").append(visita.getEstado()).append("\n");
        sb.append("Visita ID: ").append(visita.getId()).append("\n");

        txtResultado.setText(sb.toString());

        JOptionPane.showMessageDialog(this,
                "Check-in realizado correctamente.\n" +
                "Persona ID: " + visita.getPersonaId() + "\n" +
                "Empresa visitada ID: " + visita.getEmpresaVisitadaId() + "\n" +
                "Fecha/hora ingreso: " + visita.getFechaHoraIngreso(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(Throwable cause) {
        String mensaje = cause.getMessage();
        txtResultado.setText("✗ ERROR: " + mensaje);

        JOptionPane.showMessageDialog(this,
                mensaje,
                "Error en Check-in", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrar() {
        setVisible(true);
        txtDocumento.requestFocusInWindow();
    }
}