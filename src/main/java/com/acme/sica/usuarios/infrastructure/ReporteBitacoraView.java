package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.reportes.application.FiltrosBitacora;
import com.acme.sica.reportes.application.GenerarReporteBitacoraService;
import com.acme.sica.reportes.application.ReporteBitacora;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista del reporte de bitacora de auditoria con filtros.
 */
public class ReporteBitacoraView extends JFrame {

    private final Usuario usuarioActual;
    private final GenerarReporteBitacoraService service;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtFechaDesde;
    private final JTextField txtFechaHasta;
    private final JTextField txtUsuarioId;
    private final JTextField txtAccion;
    private final JLabel lblTotal;

    public ReporteBitacoraView(Usuario usuarioActual,
                                GenerarReporteBitacoraService service) {
        this.usuarioActual = usuarioActual;
        this.service = service;

        setTitle("SICA - Reporte: Bitacora de Auditoria");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        panelFiltros.add(new JLabel("Fecha desde (yyyy-MM-dd):"));
        txtFechaDesde = new JTextField(10);
        panelFiltros.add(txtFechaDesde);
        panelFiltros.add(new JLabel("Fecha hasta (yyyy-MM-dd):"));
        txtFechaHasta = new JTextField(10);
        panelFiltros.add(txtFechaHasta);
        panelFiltros.add(new JLabel("Usuario ID:"));
        txtUsuarioId = new JTextField(5);
        panelFiltros.add(txtUsuarioId);
        panelFiltros.add(new JLabel("Accion:"));
        txtAccion = new JTextField(15);
        panelFiltros.add(txtAccion);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);
        add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Fecha/Hora", "Usuario", "Accion", "Entidad", "Detalle", "Resultado"};
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

        // Inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: 0 registros");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD));
        panelInferior.add(lblTotal, BorderLayout.WEST);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        panelBotones.add(btnCerrar);
        panelInferior.add(panelBotones, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCerrar.addActionListener(e -> dispose());

        // Carga inicial sin filtros
        buscar();
    }

    private void buscar() {
        FiltrosBitacora filtros = new FiltrosBitacora();
        if (!txtFechaDesde.getText().trim().isEmpty()) {
            filtros.setFechaDesde(LocalDate.parse(txtFechaDesde.getText().trim()));
        }
        if (!txtFechaHasta.getText().trim().isEmpty()) {
            filtros.setFechaHasta(LocalDate.parse(txtFechaHasta.getText().trim()));
        }
        if (!txtUsuarioId.getText().trim().isEmpty()) {
            filtros.setUsuarioId(Long.parseLong(txtUsuarioId.getText().trim()));
        }
        if (!txtAccion.getText().trim().isEmpty()) {
            filtros.setAccion(txtAccion.getText().trim());
        }

        try {
            List<ReporteBitacora> datos = service.ejecutar(usuarioActual, filtros);
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (ReporteBitacora r : datos) {
                    tableModel.addRow(new Object[]{
                            r.getFechaHora(),
                            r.getNombreUsuario(),
                            r.getAccion(),
                            r.getEntidad(),
                            r.getDetalle(),
                            r.getResultado()
                    });
                }
                lblTotal.setText("Total: " + datos.size() + " registros");
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        txtFechaDesde.setText("");
        txtFechaHasta.setText("");
        txtUsuarioId.setText("");
        txtAccion.setText("");
        buscar();
    }

    public void mostrar() {
        setVisible(true);
    }
}
