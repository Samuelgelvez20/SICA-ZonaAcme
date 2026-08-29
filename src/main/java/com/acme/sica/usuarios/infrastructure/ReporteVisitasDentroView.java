package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.reportes.application.GenerarReporteVisitasDentroService;
import com.acme.sica.reportes.application.ReporteVisitasDentro;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vista del reporte de personas actualmente dentro del complejo.
 * Muestra una tabla con datos legibles de persona, empresa y guarda.
 */
public class ReporteVisitasDentroView extends JFrame {

    private final Usuario usuarioActual;
    private final GenerarReporteVisitasDentroService service;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblTotal;

    public ReporteVisitasDentroView(Usuario usuarioActual,
                                     GenerarReporteVisitasDentroService service) {
        this.usuarioActual = usuarioActual;
        this.service = service;

        setTitle("SICA - Reporte: Personas Dentro del Complejo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"Nombre", "Documento", "Tipo", "Empresa", "Guarda", "Hora Ingreso"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotal = new JLabel("Total: 0 personas dentro");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD));
        panelSuperior.add(lblTotal);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnCerrar = new JButton("Cerrar");
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnCerrar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> cargarDatos());
        btnCerrar.addActionListener(e -> dispose());

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<ReporteVisitasDentro> datos = service.ejecutar(usuarioActual);
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (ReporteVisitasDentro r : datos) {
                    tableModel.addRow(new Object[]{
                            r.getNombrePersona(),
                            r.getDocumentoPersona(),
                            r.getTipoPersona(),
                            r.getNombreEmpresa() != null ? r.getNombreEmpresa() : "-",
                            r.getNombreGuarda() != null ? r.getNombreGuarda() : "-",
                            r.getFechaHoraIngreso()
                    });
                }
                lblTotal.setText("Total: " + datos.size() + " personas dentro");
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrar() {
        setVisible(true);
    }
}
