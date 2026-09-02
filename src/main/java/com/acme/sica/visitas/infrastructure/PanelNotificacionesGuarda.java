package com.acme.sica.visitas.infrastructure;

import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.application.VisitaObserver;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de notificaciones persistente para el Guarda.
 *
 * <p>Combina dos fuentes de datos:
 * <ul>
 *   <li><b>Observer en memoria</b> (tiempo real): actualiza la tabla
 *       automaticamente cuando Guarda y Funcionario corren en el mismo
 *       proceso Java via {@link NotificadorVisitas}.</li>
 *   <li><b>Consulta directa a BD</b> ( boton "Actualizar"): funciona
 *       siempre, sin importar si corren en procesos separados, porque
 *       ambos apuntan a la misma base de datos PostgreSQL.</li>
 * </ul>
 *
 * <p>Implementa {@link VisitaObserver} para recibir notificaciones en
 * tiempo real. Se suscribe automaticamente al instanciarse.
 */
public class PanelNotificacionesGuarda extends JFrame implements VisitaObserver {

    private final Usuario guardaActual;
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final RegistrarCheckInService registrarCheckInService;
    private final NotificadorVisitas notificadorVisitas;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton btnCheckIn;
    private final JButton btnActualizar;

    public PanelNotificacionesGuarda(Usuario guardaActual,
                                     VisitaRepository visitaRepository,
                                     PersonaRepository personaRepository,
                                     NotificadorVisitas notificadorVisitas,
                                     RegistrarCheckInService registrarCheckInService) {
        this.guardaActual = guardaActual;
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.notificadorVisitas = notificadorVisitas;
        this.registrarCheckInService = registrarCheckInService;

        setTitle("SICA - Mis Notificaciones (" + guardaActual.getNombre() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"ID", "Persona", "Estado", "Fecha"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoBotones();
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnActualizar = new JButton("Actualizar");
        btnCheckIn = new JButton("Check-in");
        btnCheckIn.setEnabled(false);
        buttonPanel.add(btnCheckIn);
        buttonPanel.add(btnActualizar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> cargarEstadoInicial());
        btnCheckIn.addActionListener(e -> hacerCheckInSeleccionada());

        cargarEstadoInicial();
        notificadorVisitas.suscribir(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                notificadorVisitas.desuscribir(PanelNotificacionesGuarda.this);
            }
        });
    }

    private void cargarEstadoInicial() {
        btnActualizar.setEnabled(false);
        List<Visita> visitas = visitaRepository.listarPorGuarda(guardaActual.getId());
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Visita v : visitas) {
                agregarFila(v);
            }
            btnActualizar.setEnabled(true);
        });
    }

    private void agregarFila(Visita v) {
        String nombrePersona = personaRepository.buscarPorId(v.getPersonaId())
                .map(Persona::getNombre)
                .orElse("ID " + v.getPersonaId());

        String fecha;
        if (v.getFechaHoraIngreso() != null) {
            fecha = v.getFechaHoraIngreso().toString();
        } else if (v.getFechaHoraProgramada() != null) {
            fecha = v.getFechaHoraProgramada().toString();
        } else {
            fecha = "\u2014";
        }

        tableModel.addRow(new Object[]{
                v.getId(),
                nombrePersona,
                v.getEstado().name(),
                fecha
        });
    }

    private void actualizarEstadoBotones() {
        int row = table.getSelectedRow();
        if (row < 0) {
            btnCheckIn.setEnabled(false);
            return;
        }
        String estado = (String) tableModel.getValueAt(row, 2);
        btnCheckIn.setEnabled("APROBADA".equals(estado));
    }

    private void hacerCheckInSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Long visitaId = (Long) tableModel.getValueAt(row, 0);
        var visitaOpt = visitaRepository.buscarPorId(visitaId);
        if (visitaOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Visita no encontrada",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Visita visita = visitaOpt.get();
        var personaOpt = personaRepository.buscarPorId(visita.getPersonaId());
        if (personaOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontro la persona (ID " + visita.getPersonaId() + ")",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String documento = personaOpt.get().getDocumento();
            registrarCheckInService.ejecutar(guardaActual, documento);
            JOptionPane.showMessageDialog(this, "Check-in realizado correctamente",
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
            cargarEstadoInicial();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en check-in: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onSolicitudPendiente(Visita visita) {
        // El guarda no necesita ver nuevas solicitudes de otros guardas
    }

    @Override
    public void onDecisionTomada(Visita visita) {
        if (visita.getGuardaId() != null && visita.getGuardaId().equals(guardaActual.getId())) {
            SwingUtilities.invokeLater(this::cargarEstadoInicial);
        }
    }

    public void cerrar() {
        notificadorVisitas.desuscribir(this);
        dispose();
    }
}
