package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vista de gestion de personas.
 * Muestra lista de personas y permite crear/editar.
 */
public class PersonasView extends JFrame {

    private final Usuario usuarioActual;
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final CrearPersonaService crearPersonaService;
    private final ActualizarPersonaService actualizarPersonaService;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public PersonasView(Usuario usuarioActual,
                         PersonaRepository personaRepository,
                         EmpresaRepository empresaRepository,
                         CrearPersonaService crearPersonaService,
                         ActualizarPersonaService actualizarPersonaService) {
        this.usuarioActual = usuarioActual;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.crearPersonaService = crearPersonaService;
        this.actualizarPersonaService = actualizarPersonaService;

        setTitle("SICA - Gestion de Personas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"ID", "Nombre", "Documento", "Tipo", "Empresa ID", "Bloqueado"};
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
        JButton btnCrear = new JButton("Crear Persona");
        JButton btnRefrescar = new JButton("Refrescar");
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnRefrescar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearPersona());
        btnRefrescar.addActionListener(e -> cargarDatos());

        cargarDatos();
    }

    private void cargarDatos() {
        List<Persona> personas = personaRepository.listarTodas();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Persona p : personas) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getNombre(), p.getDocumento(),
                        p.getTipo().name(),
                        p.getEmpresaId() != null ? p.getEmpresaId() : "",
                        p.isBloqueado() ? "SI" : "NO"
                });
            }
        });
    }

    private void crearPersona() {
        JTextField txtNombre = new JTextField();
        JTextField txtDocumento = new JTextField();
        JComboBox<TipoPersona> cmbTipo = new JComboBox<>(TipoPersona.values());
        JTextField txtEmpresaId = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Documento:"));
        panel.add(txtDocumento);
        panel.add(new JLabel("Tipo:"));
        panel.add(cmbTipo);
        panel.add(new JLabel("Empresa ID (vacio si no aplica):"));
        panel.add(txtEmpresaId);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Persona",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String documento = txtDocumento.getText().trim();
                TipoPersona tipo = (TipoPersona) cmbTipo.getSelectedItem();
                Long empresaId = txtEmpresaId.getText().trim().isEmpty()
                        ? null : Long.parseLong(txtEmpresaId.getText().trim());
                crearPersonaService.ejecutar(usuarioActual, nombre, documento, tipo,
                        null, empresaId, null);
                JOptionPane.showMessageDialog(this, "Persona creada correctamente",
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
