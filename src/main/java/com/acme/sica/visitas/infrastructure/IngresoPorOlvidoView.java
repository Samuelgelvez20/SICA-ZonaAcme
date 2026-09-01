package com.acme.sica.visitas.infrastructure;

import com.acme.sica.visitas.application.RegistrarIngresoPorOlvidoService;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import java.awt.*;


/**
 * Pantalla simple para que el Guarda registre un ingreso por carnet olvidado.
 *
 * <p>Un campo (documento) + botón. Usa SwingWorker para no bloquear la UI.
 */
public class IngresoPorOlvidoView extends JPanel {

    private final Usuario guardaActual;
    private final RegistrarIngresoPorOlvidoService registrarService;

    private final JTextField txtDocumento;
    private final JButton btnRegistrar;
    private final JLabel lblStatus;

    public IngresoPorOlvidoView(Usuario guardaActual,
                                RegistrarIngresoPorOlvidoService registrarService) {
        this.guardaActual = guardaActual;
        this.registrarService = registrarService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Ingreso por Carnet Olvidado - " + guardaActual.getNombre()));

        // Panel de entrada
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.add(new JLabel("Documento:"));
        txtDocumento = new JTextField(20);
        inputPanel.add(txtDocumento);
        btnRegistrar = new JButton("Registrar");
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
            JOptionPane.showMessageDialog(this, "El documento es obligatorio", "Aviso", JOptionPane.WARNING_MESSAGE);
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
                    lblStatus.setText("Solicitud enviada (ID: " + visita.getId() + "), esperando aprobación");
                    JOptionPane.showMessageDialog(
                            IngresoPorOlvidoView.this,
                            "Solicitud enviada al funcionario anfitri\u00f3n.\n" +
                            "Estado: PENDIENTE_APROBACION_OLVIDO\n" +
                            "En espera de aprobaci\u00f3n...",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    txtDocumento.setText("");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(
                            IngresoPorOlvidoView.this,
                            "Error: " + cause.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}