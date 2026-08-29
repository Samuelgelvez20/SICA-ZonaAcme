package com.acme.sica.visitas.infrastructure;

import com.acme.sica.visitas.application.AprobarORechazarVisitaService;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.VisitaObserver;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Pantalla del Funcionario para ver y gestionar solicitudes pendientes.
 *
 * <p>Implementa {@link VisitaObserver} para recibir notificaciones en tiempo real
 * de nuevas solicitudes y decisiones tomadas.
 */
public class FuncionarioPendientesView extends JFrame implements VisitaObserver {

    private final Usuario funcionarioActual;
    private final VisitaRepository visitaRepository;
    private final AprobarORechazarVisitaService aprobarORechazarService;
    private final NotificadorVisitas notificadorVisitas;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnAprobar;
    private final JButton btnRechazar;
    private final JButton btnRefrescar;

    public FuncionarioPendientesView(Usuario funcionarioActual,
                                     VisitaRepository visitaRepository,
                                     AprobarORechazarVisitaService aprobarORechazarService,
                                     NotificadorVisitas notificadorVisitas) {
        this.funcionarioActual = funcionarioActual;
        this.visitaRepository = visitaRepository;
        this.aprobarORechazarService = aprobarORechazarService;
        this.notificadorVisitas = notificadorVisitas;

        setTitle("SICA - Solicitudes Pendientes (" + funcionarioActual.getNombre() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Tabla
        String[] columnas = {"ID", "Persona", "Documento", "Empresa", "Fecha Solicitud", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnAprobar = new JButton("Aprobar");
        btnRechazar = new JButton("Rechazar");
        btnRefrescar = new JButton("Refrescar");
        buttonPanel.add(btnAprobar);
        buttonPanel.add(btnRechazar);
        buttonPanel.add(btnRefrescar);
        add(buttonPanel, BorderLayout.SOUTH);

        // Acciones
        btnAprobar.addActionListener(e -> aprobarSeleccionada());
        btnRechazar.addActionListener(e -> rechazarSeleccionada());
        btnRefrescar.addActionListener(e -> cargarEstadoInicial());

        // Cargar estado inicial y suscribirse
        cargarEstadoInicial();
        notificadorVisitas.suscribir(this);

        // Cleanup al cerrar
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                notificadorVisitas.desuscribir(FuncionarioPendientesView.this);
                dispose();
            }
        });
    }

    private void cargarEstadoInicial() {
        List<Visita> pendientes = visitaRepository.listarPendientesPorFuncionario(funcionarioActual.getId());
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Visita v : pendientes) {
                agregarFila(v);
            }
        });
    }

private void agregarFila(Visita v) {
        tableModel.addRow(new Object[]{
                v.getId(),
                v.getPersonaId(), // se podr\u00eda resolver nombre con join, simplificado
                "doc-" + v.getPersonaId(), // placeholder
                v.getEmpresaVisitadaId() != null ? "emp-" + v.getEmpresaVisitadaId() : "\u2014",
                "reciente", // creadoEn no est\u00e1 en el dominio, se muestra placeholder
                v.getEstado().name()
        });
    }

    private Visita obtenerVisitaSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Long visitaId = (Long) tableModel.getValueAt(row, 0);
        return visitaRepository.buscarPorId(visitaId).orElse(null);
    }

    private void aprobarSeleccionada() {
        Visita visita = obtenerVisitaSeleccionada();
        if (visita == null) return;

        try {
            aprobarORechazarService.aprobar(funcionarioActual, visita.getId());
            JOptionPane.showMessageDialog(this, "Solicitud aprobada correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            // La fila se remueve vía onDecisionTomada (observer)
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechazarSeleccionada() {
        Visita visita = obtenerVisitaSeleccionada();
        if (visita == null) return;

        String motivo = JOptionPane.showInputDialog(this, "Motivo del rechazo:", "Rechazar solicitud", JOptionPane.QUESTION_MESSAGE);
        if (motivo == null || motivo.isBlank()) {
            return; // usuario canceló
        }

        try {
            aprobarORechazarService.rechazar(funcionarioActual, visita.getId(), motivo);
            JOptionPane.showMessageDialog(this, "Solicitud rechazada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            // La fila se remueve vía onDecisionTomada (observer)
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onSolicitudPendiente(Visita visita) {
        // Solo nos interesan las solicitudes para ESTE funcionario
        if (visita.getFuncionarioId() != null && visita.getFuncionarioId().equals(funcionarioActual.getId())) {
            SwingUtilities.invokeLater(() -> agregarFila(visita));
        }
    }

    @Override
    public void onDecisionTomada(Visita visita) {
        // Si la decisión fue sobre una solicitud de este funcionario, quitar la fila
        if (visita.getFuncionarioId() != null && visita.getFuncionarioId().equals(funcionarioActual.getId())) {
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(visita.getId())) {
                        tableModel.removeRow(i);
                        break;
                    }
                }
            });
        }
    }
}