package com.acme.sica.visitas.infrastructure;

import com.acme.sica.visitas.application.RegistrarIngresoTrabajadorService;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Vista para ingreso directo de trabajadores con carnet.
 *
 * <p>Un campo (documento) + botón. Usa SwingWorker para no bloquear la UI.
 */
public class IngresoTrabajadorView extends JPanel {

    private final Usuario guardaActual;
    private final RegistrarIngresoTrabajadorService registrarService;

    private final JTextField txtDocumento;
    private final JButton btnRegistrar;
    private final JLabel lblStatus;

    public IngresoTrabajadorView(Usuario guardaActual,
                                  RegistrarIngresoTrabajadorService registrarService) {
        this.guardaActual = guardaActual;
        this.registrarService = registrarService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Ingreso de Trabajador - " + guardaActual.getNombre()));

        // Panel de entrada
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.add(new JLabel("Documento:"));
        txtDocumento = new JTextField(20);
        inputPanel.add(txtDocumento);
        btnRegistrar = new JButton("Registrar Ingreso");
        inputPanel.add(btnRegistrar);
        add(inputPanel, BorderLayout.NORTH);

        // Status
        lblStatus = new JLabel("Ingrese documento del trabajador");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblStatus, BorderLayout.CENTER);

        // Acción
        btnRegistrar.addActionListener(e -> registrar());
    }

    private void registrar() {
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El documento es obligatorio",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnRegistrar.setEnabled(false);
        lblStatus.setText("Procesando...");

        new SwingWorker<Visita, Void>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return registrarService.registrar(guardaActual, documento);
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    Visita visita = get();
                    lblStatus.setText("Ingreso registrado (ID: " + visita.getId() + ")");
                    JOptionPane.showMessageDialog(
                            IngresoTrabajadorView.this,
                            "Ingreso de trabajador registrado correctamente.\n" +
                            "Estado: DENTRO\n" +
                            "Visita ID: " + visita.getId(),
                            "Éxito", JOptionPane.INFORMATION_MESSAGE
                    );
                    txtDocumento.setText("");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(
                            IngresoTrabajadorView.this,
                            "Error: " + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}
