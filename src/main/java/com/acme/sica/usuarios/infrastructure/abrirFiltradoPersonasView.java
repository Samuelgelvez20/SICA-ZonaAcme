package com.acme.sica.usuarios.infrastructure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.reportes.application.GenerarReporteVisitasDentroService;
import com.acme.sica.reportes.application.ReporteVisitasDentro;
import com.acme.sica.usuarios.domain.Usuario;




public class abrirFiltradoPersonasView extends JFrame{
    private final Usuario usuarioActual;
    private final EmpresaRepository empresaRepository;
    private final GenerarReporteVisitasDentroService service;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblTotal;
    private JComboBox<com.acme.sica.personas.domain.Empresa> cmbEmpresaFiltro;

    public abrirFiltradoPersonasView(Usuario usuarioActual,
                         PersonaRepository personaRepository,
                         EmpresaRepository empresaRepository,
                         GenerarReporteVisitasDentroService service,
                         ListarPersonasPorEmpresaService listarPersonasPorEmpresaService) {
        this.usuarioActual = usuarioActual;
        this.empresaRepository = empresaRepository;
        this.service = service;

        setTitle("SICA - Personas Dentro Del Complejo Por Empresa");
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


        // Panel superior: filtro por empresa
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setBorder(BorderFactory.createTitledBorder("Filtro"));
        panelFiltro.add(new JLabel("Empresa:"));
        cmbEmpresaFiltro = new JComboBox<>();
        cmbEmpresaFiltro.addItem(null); // "Todas"
        cmbEmpresaFiltro.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof com.acme.sica.personas.domain.Empresa empresa) {
                    setText(empresa.getNombre());
                } else if (value == null) {
                    setText("Todas");
                }
                return this;
            }
        });
        cargarEmpresasEnFiltro();
        panelFiltro.add(cmbEmpresaFiltro);
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> cargarDatos());
        panelFiltro.add(btnFiltrar);
        add(panelFiltro, BorderLayout.NORTH);

        // Panel inferior: botones
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

    private void cargarEmpresasEnFiltro() {
        List<com.acme.sica.personas.domain.Empresa> empresas = empresaRepository.listarTodas();
        for (com.acme.sica.personas.domain.Empresa e : empresas) {
            cmbEmpresaFiltro.addItem(e);
        }
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
