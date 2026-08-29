package com.acme.sica.visitas.infrastructure;

import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.application.RegistrarCheckOutService;
import com.acme.sica.visitas.domain.Visita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Pantalla de Check-out para el Guarda.
 *
 * <p>Estructura gemela a CheckInView: campo documento + botón, SwingWorker,
 * JOptionPane de resultado.
 */
public class CheckOutView extends JFrame {

    private final RegistrarCheckOutService checkOutService;
    private final Usuario usuarioActual;

    private JTextField txtDocumento;
    private JButton btnCheckOut;
    private JTextArea txtResultado;

    public CheckOutView(RegistrarCheckOutService checkOutService, Usuario usuarioActual) {
        this.checkOutService = checkOutService;
        this.usuarioActual = usuarioActual;
        initComponents();
    }

    private void initComponents() {
        setTitle("SICA - Check-out de Visitante (Guarda: " + usuarioActual.getNombre() + ")");
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

        btnCheckOut = new JButton("Buscar y Check-out");
        btnCheckOut.addActionListener(this::onCheckOut);
        panelCampos.add(btnCheckOut);

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

        // Enter en el campo dispara el check-out
        txtDocumento.addActionListener(this::onCheckOut);
    }

    private void onCheckOut(ActionEvent e) {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese un documento para buscar.",
                    "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnCheckOut.setEnabled(false);
        txtResultado.setText("Buscando...");

        // Ejecutar en hilo de fondo para no bloquear la UI
        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() {
                return checkOutService.registrar(usuarioActual, documento);
            }

            @Override
            protected void done() {
                btnCheckOut.setEnabled(true);
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
        StringBuilder sb = new StringBuilder();
        sb.append("✓ CHECK-OUT EXITOSO\n\n");
        sb.append("Persona: ").append(visita.getPersonaId()).append("\n");
        sb.append("Empresa visitada ID: ").append(visita.getEmpresaVisitadaId()).append("\n");
        sb.append("Fecha/hora salida: ").append(visita.getFechaHoraSalida()).append("\n");
        sb.append("Estado: ").append(visita.getEstado()).append("\n");
        sb.append("Visita ID: ").append(visita.getId()).append("\n");

        txtResultado.setText(sb.toString());

        JOptionPane.showMessageDialog(this,
                "Check-out realizado correctamente.\n" +
                "Persona ID: " + visita.getPersonaId() + "\n" +
                "Empresa visitada ID: " + visita.getEmpresaVisitadaId() + "\n" +
                "Fecha/hora salida: " + visita.getFechaHoraSalida(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(Throwable cause) {
        String mensaje = cause.getMessage();
        txtResultado.setText("✗ ERROR: " + mensaje);

        JOptionPane.showMessageDialog(this,
                mensaje,
                "Error en Check-out", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrar() {
        setVisible(true);
        txtDocumento.requestFocusInWindow();
    }
}