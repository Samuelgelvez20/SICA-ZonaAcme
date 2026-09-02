package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
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
    private final ListarPersonasPorEmpresaService listarPersonasPorEmpresaService;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private JComboBox<com.acme.sica.personas.domain.Empresa> cmbEmpresaFiltro;

    public PersonasView(Usuario usuarioActual,
                         PersonaRepository personaRepository,
                         EmpresaRepository empresaRepository,
                         CrearPersonaService crearPersonaService,
                         ActualizarPersonaService actualizarPersonaService,
                         ListarPersonasPorEmpresaService listarPersonasPorEmpresaService) {
        this.usuarioActual = usuarioActual;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.crearPersonaService = crearPersonaService;
        this.actualizarPersonaService = actualizarPersonaService;
        this.listarPersonasPorEmpresaService = listarPersonasPorEmpresaService;

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

        // Panel superior: filtro por empresa
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setBorder(BorderFactory.createTitledBorder("Filtro"));
        panelFiltro.add(new JLabel("Empresa:"));
        cmbEmpresaFiltro = new JComboBox<>();
        cmbEmpresaFiltro.addItem(null); // "Todas"
        cargarEmpresasEnFiltro();
        panelFiltro.add(cmbEmpresaFiltro);
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> cargarDatos());
        panelFiltro.add(btnFiltrar);
        add(panelFiltro, BorderLayout.NORTH);

        // Panel inferior: botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCrear = new JButton("Crear Persona");
        JButton btnEditar = new JButton("Editar");
        JButton btnRefrescar = new JButton("Refrescar");
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnRefrescar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearPersona());
        btnEditar.addActionListener(e -> editarPersona());
        btnRefrescar.addActionListener(e -> cargarDatos());

        cargarDatos();
    }

    private void cargarEmpresasEnFiltro() {
        List<com.acme.sica.personas.domain.Empresa> empresas = empresaRepository.listarTodas();
        for (com.acme.sica.personas.domain.Empresa e : empresas) {
            cmbEmpresaFiltro.addItem(e);
        }
    }

    private void cargarDatos() {
        com.acme.sica.personas.domain.Empresa empresaSeleccionada = (com.acme.sica.personas.domain.Empresa) cmbEmpresaFiltro.getSelectedItem();
        List<Persona> personas;
        if (empresaSeleccionada != null) {
            personas = listarPersonasPorEmpresaService.ejecutar(usuarioActual, empresaSeleccionada.getId());
        } else {
            personas = personaRepository.listarTodas();
        }
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

    private void editarPersona() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una persona para editar",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long personaId = (Long) tableModel.getValueAt(selectedRow, 0);
        var personaOpt = personaRepository.buscarPorId(personaId);
        if (personaOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontró la persona",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Persona persona = personaOpt.get();

        JTextField txtNombre = new JTextField(persona.getNombre());
        JTextField txtDocumento = new JTextField(persona.getDocumento());
        JComboBox<TipoPersona> cmbTipo = new JComboBox<>(TipoPersona.values());
        cmbTipo.setSelectedItem(persona.getTipo());
        JTextField txtFotoUrl = new JTextField(persona.getFotoUrl() != null ? persona.getFotoUrl() : "");
        JTextField txtEmpresaId = new JTextField(persona.getEmpresaId() != null ? persona.getEmpresaId().toString() : "");
        JTextField txtFuncionarioAnfitrionId = new JTextField(persona.getFuncionarioAnfitrionId() != null ? persona.getFuncionarioAnfitrionId().toString() : "");
        JCheckBox chkBloqueado = new JCheckBox("Bloqueado", persona.isBloqueado());
        JTextField txtMotivoBloqueo = new JTextField(persona.getMotivoBloqueo() != null ? persona.getMotivoBloqueo() : "");

        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Documento:"));
        panel.add(txtDocumento);
        panel.add(new JLabel("Tipo:"));
        panel.add(cmbTipo);
        panel.add(new JLabel("Foto URL:"));
        panel.add(txtFotoUrl);
        panel.add(new JLabel("Empresa ID (vacío si no aplica):"));
        panel.add(txtEmpresaId);
        panel.add(new JLabel("Funcionario Anfitrión ID (vacío si no aplica):"));
        panel.add(txtFuncionarioAnfitrionId);
        panel.add(new JLabel("Bloqueado:"));
        panel.add(chkBloqueado);
        panel.add(new JLabel("Motivo Bloqueo:"));
        panel.add(txtMotivoBloqueo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Persona",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                String documento = txtDocumento.getText().trim();
                TipoPersona tipo = (TipoPersona) cmbTipo.getSelectedItem();
                String fotoUrl = txtFotoUrl.getText().trim();
                String empresaIdStr = txtEmpresaId.getText().trim();
                String funcionarioAnfitrionIdStr = txtFuncionarioAnfitrionId.getText().trim();
                boolean bloqueado = chkBloqueado.isSelected();
                String motivoBloqueo = txtMotivoBloqueo.getText().trim();

                if (nombre.isEmpty() || documento.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nombre y documento son obligatorios",
                            "Campos requeridos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Long empresaId = empresaIdStr.isEmpty() ? null : Long.parseLong(empresaIdStr);
                Long funcionarioAnfitrionId = funcionarioAnfitrionIdStr.isEmpty() ? null : Long.parseLong(funcionarioAnfitrionIdStr);

                Persona personaActualizada = new Persona(
                        persona.getId(),
                        nombre,
                        documento,
                        tipo,
                        fotoUrl.isEmpty() ? null : fotoUrl,
                        empresaId,
                        funcionarioAnfitrionId,
                        bloqueado,
                        motivoBloqueo.isEmpty() ? null : motivoBloqueo
                );

                actualizarPersonaService.ejecutar(usuarioActual, personaActualizada);
                JOptionPane.showMessageDialog(this, "Persona actualizada correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID de empresa o funcionario anfitrión inválido",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                JOptionPane.showMessageDialog(this, "Error: " + cause.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
        panel.add(new JLabel("Empresa ID (vacío si no aplica):"));
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
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                JOptionPane.showMessageDialog(this, "Error: " + cause.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void mostrar() {
        setVisible(true);
    }
}
