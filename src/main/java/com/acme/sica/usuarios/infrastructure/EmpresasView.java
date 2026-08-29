package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.CrearEmpresaService;
import com.acme.sica.personas.domain.Empresa;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vista de gestion de empresas.
 * Muestra lista de empresas y permite crear.
 */
public class EmpresasView extends JFrame {

    private final Usuario usuarioActual;
    private final EmpresaRepository empresaRepository;
    private final CrearEmpresaService crearEmpresaService;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public EmpresasView(Usuario usuarioActual,
                         EmpresaRepository empresaRepository,
                         CrearEmpresaService crearEmpresaService) {
        this.usuarioActual = usuarioActual;
        this.empresaRepository = empresaRepository;
        this.crearEmpresaService = crearEmpresaService;

        setTitle("SICA - Gestion de Empresas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"ID", "Nombre", "NIT"};
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCrear = new JButton("Crear Empresa");
        JButton btnRefrescar = new JButton("Refrescar");
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnRefrescar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearEmpresa());
        btnRefrescar.addActionListener(e -> cargarDatos());

        cargarDatos();
    }

    private void cargarDatos() {
        List<Empresa> empresas = empresaRepository.listarTodas();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Empresa e : empresas) {
                tableModel.addRow(new Object[]{e.getId(), e.getNombre(), e.getNit()});
            }
        });
    }

    private void crearEmpresa() {
        JTextField txtNombre = new JTextField();
        JTextField txtNit = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("NIT:"));
        panel.add(txtNit);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Empresa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String nit = txtNit.getText().trim();
                crearEmpresaService.ejecutar(usuarioActual, nombre, nit);
                JOptionPane.showMessageDialog(this, "Empresa creada correctamente",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void mostrar() {
        setVisible(true);
    }
}
