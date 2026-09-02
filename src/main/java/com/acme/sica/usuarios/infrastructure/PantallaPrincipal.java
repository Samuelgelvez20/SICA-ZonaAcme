package com.acme.sica.usuarios.infrastructure;

import com.acme.sica.incidentes.application.IncidenteRepository;
import com.acme.sica.incidentes.application.RegistrarIncidenteService;
import com.acme.sica.personas.application.BloquearPersonaService;
import com.acme.sica.personas.application.EmpresaRepository;
import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.application.CrearEmpresaService;
import com.acme.sica.personas.application.CrearPersonaService;
import com.acme.sica.personas.application.ActualizarPersonaService;
import com.acme.sica.personas.application.ListarPersonasPorEmpresaService;
import com.acme.sica.reportes.application.GenerarReporteBitacoraService;
import com.acme.sica.reportes.application.GenerarReporteVisitasDentroService;
import com.acme.sica.usuarios.application.UsuarioRepository;
import com.acme.sica.usuarios.domain.Usuario;
import com.acme.sica.visitas.application.AprobarORechazarVisitaService;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.PreRegistrarInvitadoService;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.application.RegistrarCheckOutService;
import com.acme.sica.visitas.application.RegistrarIngresoPorOlvidoService;
import com.acme.sica.visitas.application.RegistrarIngresoTrabajadorService;
import com.acme.sica.visitas.application.RegistrarVisitaNoAnunciadaService;
import com.acme.sica.visitas.application.VisitaRepository;
import com.acme.sica.visitas.infrastructure.CheckInView;
import com.acme.sica.visitas.infrastructure.CheckOutView;
import com.acme.sica.visitas.infrastructure.IngresoPorOlvidoView;
import com.acme.sica.visitas.infrastructure.FuncionarioPendientesView;
import com.acme.sica.visitas.infrastructure.IngresoTrabajadorView;
import com.acme.sica.visitas.infrastructure.PanelEsperaGuarda;
import com.acme.sica.visitas.infrastructure.RegistroNoAnunciadoView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Pantalla principal de navegación condicionada por rol.
 * Muestra botones diferentes según el rol del usuario autenticado.
 */
public class PantallaPrincipal extends JFrame {

    private final Usuario usuarioActual;
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VisitaRepository visitaRepository;
    private final NotificadorVisitas notificadorVisitas;

    private final RegistrarCheckInService registrarCheckInService;
    private final RegistrarCheckOutService registrarCheckOutService;
    private final RegistrarIngresoPorOlvidoService registrarIngresoPorOlvidoService;
    private final PreRegistrarInvitadoService preRegistrarInvitadoService;
    private final AprobarORechazarVisitaService aprobarORechazarService;
    private final CrearPersonaService crearPersonaService;
    private final CrearEmpresaService crearEmpresaService;
    private final ActualizarPersonaService actualizarPersonaService;
    private final ListarPersonasPorEmpresaService listarPersonasPorEmpresaService;
    private final RegistrarIncidenteService registrarIncidenteService;
    private final BloquearPersonaService bloquearPersonaService;
    private final GenerarReporteVisitasDentroService generarReporteVisitasDentroService;
    private final GenerarReporteBitacoraService generarReporteBitacoraService;
    private final RegistrarVisitaNoAnunciadaService registrarVisitaNoAnunciadaService;
    private final RegistrarIngresoTrabajadorService registrarIngresoTrabajadorService;

    private PanelEsperaGuarda panelEsperaGuarda;

    public PantallaPrincipal(Usuario usuarioActual,
                              PersonaRepository personaRepository,
                              EmpresaRepository empresaRepository,
                              UsuarioRepository usuarioRepository,
                              VisitaRepository visitaRepository,
                              NotificadorVisitas notificadorVisitas,
                              RegistrarCheckInService registrarCheckInService,
                              RegistrarCheckOutService registrarCheckOutService,
                              RegistrarIngresoPorOlvidoService registrarIngresoPorOlvidoService,
                              PreRegistrarInvitadoService preRegistrarInvitadoService,
                              AprobarORechazarVisitaService aprobarORechazarService,
                              CrearPersonaService crearPersonaService,
                              CrearEmpresaService crearEmpresaService,
                              ActualizarPersonaService actualizarPersonaService,
                              ListarPersonasPorEmpresaService listarPersonasPorEmpresaService,
                              RegistrarIncidenteService registrarIncidenteService,
                              BloquearPersonaService bloquearPersonaService,
                              GenerarReporteVisitasDentroService generarReporteVisitasDentroService,
                              GenerarReporteBitacoraService generarReporteBitacoraService,
                              RegistrarVisitaNoAnunciadaService registrarVisitaNoAnunciadaService,
                              RegistrarIngresoTrabajadorService registrarIngresoTrabajadorService) {
        this.usuarioActual = usuarioActual;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.visitaRepository = visitaRepository;
        this.notificadorVisitas = notificadorVisitas;
        this.registrarCheckInService = registrarCheckInService;
        this.registrarCheckOutService = registrarCheckOutService;
        this.registrarIngresoPorOlvidoService = registrarIngresoPorOlvidoService;
        this.preRegistrarInvitadoService = preRegistrarInvitadoService;
        this.aprobarORechazarService = aprobarORechazarService;
        this.crearPersonaService = crearPersonaService;
        this.crearEmpresaService = crearEmpresaService;
        this.actualizarPersonaService = actualizarPersonaService;
        this.listarPersonasPorEmpresaService = listarPersonasPorEmpresaService;
        this.registrarIncidenteService = registrarIncidenteService;
        this.bloquearPersonaService = bloquearPersonaService;
        this.generarReporteVisitasDentroService = generarReporteVisitasDentroService;
        this.generarReporteBitacoraService = generarReporteBitacoraService;
        this.registrarVisitaNoAnunciadaService = registrarVisitaNoAnunciadaService;
        this.registrarIngresoTrabajadorService = registrarIngresoTrabajadorService;

        initUI();
    }

    private void initUI() {
        setTitle("SICA - " + usuarioActual.getRol().getNombre() + " - " + usuarioActual.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior: info del usuario
        JLabel lblUsuario = new JLabel("Usuario: " + usuarioActual.getNombre()
                + " | Rol: " + usuarioActual.getRol().getNombre());
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD, 14f));
        add(lblUsuario, BorderLayout.NORTH);

        // Panel central: botones de navegación
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(0, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String rol = usuarioActual.getRol().getNombre();

        if (rol.equals("GUARDA") || rol.equals("ADMIN")) {
            addBoton(panelBotones, "Check-in", e -> abrirCheckIn());
            addBoton(panelBotones, "Check-out", e -> abrirCheckOut());
            addBoton(panelBotones, "Ingreso por Olvido", e -> abrirIngresoPorOlvido());
            addBoton(panelBotones, "Ingreso No Anunciado", e -> abrirIngresoNoAnunciado());
            addBoton(panelBotones, "Ingreso de Trabajador", e -> abrirIngresoTrabajador());
        }

        if (rol.equals("FUNCIONARIO") || rol.equals("ADMIN")) {
            addBoton(panelBotones, "Solicitudes Pendientes", e -> abrirPendientes());
            addBoton(panelBotones, "Pre-registrar Invitado", e -> abrirPreRegistro());
            addBoton(panelBotones, "Registrar Incidente", e -> abrirIncidente());
            addBoton(panelBotones, "Bloquear Persona", e -> abrirBloqueo());
        }

        // editar_persona permission: GUARDA, FUNCIONARIO, ADMIN (per data.sql)
        if (rol.equals("GUARDA") || rol.equals("FUNCIONARIO") || rol.equals("ADMIN")) {
            addBoton(panelBotones, "Gestionar Personas", e -> abrirPersonas());
            addBoton(panelBotones, "Gestionar Empresas", e -> abrirEmpresas());
        }

        if (rol.equals("FUNCIONARIO") || rol.equals("ADMIN")) {
            addBoton(panelBotones, "Reporte: Personas Dentro", e -> abrirReporteDentro());
            addBoton(panelBotones, "Reporte: Bitácora", e -> abrirReporteBitacora());
        }

        add(panelBotones, BorderLayout.CENTER);

        // Panel de espera para GUARDA/ADMIN (HU-10)
        if (rol.equals("GUARDA") || rol.equals("ADMIN")) {
            panelEsperaGuarda = new PanelEsperaGuarda(usuarioActual, notificadorVisitas,
                    registrarCheckInService, personaRepository);
            add(panelEsperaGuarda, BorderLayout.SOUTH);
        }

        // Panel inferior: cerrar sesión
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);

        // Cleanup al cerrar ventana principal
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panelEsperaGuarda != null) {
                    panelEsperaGuarda.cerrar();
                }
            }
        });
    }

    private void addBoton(JPanel panel, String texto, java.awt.event.ActionListener action) {
        JButton btn = new JButton(texto);
        btn.setFont(btn.getFont().deriveFont(13f));
        btn.setPreferredSize(new Dimension(200, 50));
        btn.addActionListener(action);
        panel.add(btn);
    }

    private void abrirCheckIn() {
        new CheckInView(registrarCheckInService, usuarioActual).mostrar();
    }

    private void abrirCheckOut() {
        new CheckOutView(registrarCheckOutService, usuarioActual).mostrar();
    }

    private void abrirIngresoPorOlvido() {
        JFrame frame = new JFrame("SICA - Ingreso por Carnet Olvidado");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLocationRelativeTo(null);
        frame.add(new IngresoPorOlvidoView(usuarioActual, registrarIngresoPorOlvidoService));
        frame.setVisible(true);
    }

    private void abrirIngresoNoAnunciado() {
        JFrame frame = new JFrame("SICA - Ingreso No Anunciado");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.add(new RegistroNoAnunciadoView(usuarioActual, registrarVisitaNoAnunciadaService,
                personaRepository, usuarioRepository));
        frame.setVisible(true);
    }

    private void abrirIngresoTrabajador() {
        JFrame frame = new JFrame("SICA - Ingreso de Trabajador");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLocationRelativeTo(null);
        frame.add(new IngresoTrabajadorView(usuarioActual, registrarIngresoTrabajadorService));
        frame.setVisible(true);
    }

    private void abrirPendientes() {
        new FuncionarioPendientesView(usuarioActual, visitaRepository,
                personaRepository, aprobarORechazarService, notificadorVisitas).setVisible(true);
    }

    private void abrirPreRegistro() {
        JFrame frame = new JFrame("SICA - Pre-registrar Invitado");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtPersonaId = new JTextField();
        JTextField txtEmpresaId = new JTextField();
        JTextField txtFecha = new JTextField();
        JButton btnRegistrar = new JButton("Pre-registrar");
        panel.add(new JLabel("Persona ID:"));
        panel.add(txtPersonaId);
        panel.add(new JLabel("Empresa Visitada ID:"));
        panel.add(txtEmpresaId);
        panel.add(new JLabel("Fecha (yyyy-MM-dd HH:mm):"));
        panel.add(txtFecha);
        panel.add(new JLabel(""));
        panel.add(btnRegistrar);
        btnRegistrar.addActionListener(e -> {
            try {
                Long personaId = Long.parseLong(txtPersonaId.getText().trim());
                Long empId = Long.parseLong(txtEmpresaId.getText().trim());
                java.time.LocalDateTime fecha = java.time.LocalDateTime.parse(txtFecha.getText().trim(),
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                preRegistrarInvitadoService.ejecutar(usuarioActual, personaId, empId, fecha);
                JOptionPane.showMessageDialog(frame, "Invitado pre-registrado correctamente",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.add(panel);
        frame.setVisible(true);
    }

    private void abrirIncidente() {
        JFrame frame = new JFrame("SICA - Registrar Incidente");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtPersonaId = new JTextField();
        JTextField txtTipo = new JTextField();
        JTextField txtDescripcion = new JTextField();
        JButton btnRegistrar = new JButton("Registrar Incidente");
        panel.add(new JLabel("Persona ID:"));
        panel.add(txtPersonaId);
        panel.add(new JLabel("Tipo:"));
        panel.add(txtTipo);
        panel.add(new JLabel("Descripcion:"));
        panel.add(txtDescripcion);
        panel.add(new JLabel(""));
        panel.add(btnRegistrar);
        btnRegistrar.addActionListener(e -> {
            try {
                Long personaId = Long.parseLong(txtPersonaId.getText().trim());
                String tipo = txtTipo.getText().trim();
                String desc = txtDescripcion.getText().trim();
                registrarIncidenteService.ejecutar(usuarioActual, personaId, tipo, desc);
                JOptionPane.showMessageDialog(frame, "Incidente registrado correctamente",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.add(panel);
        frame.setVisible(true);
    }

    private void abrirBloqueo() {
        JFrame frame = new JFrame("SICA - Bloquear Persona");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtPersonaId = new JTextField();
        JTextField txtMotivo = new JTextField();
        JButton btnBloquear = new JButton("Bloquear Persona");
        panel.add(new JLabel("Persona ID:"));
        panel.add(txtPersonaId);
        panel.add(new JLabel("Motivo:"));
        panel.add(txtMotivo);
        panel.add(new JLabel(""));
        panel.add(btnBloquear);
        btnBloquear.addActionListener(e -> {
            try {
                Long personaId = Long.parseLong(txtPersonaId.getText().trim());
                String motivo = txtMotivo.getText().trim();
                bloquearPersonaService.ejecutar(usuarioActual, personaId, motivo);
                JOptionPane.showMessageDialog(frame, "Persona bloqueada correctamente",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.add(panel);
        frame.setVisible(true);
    }

    private void abrirPersonas() {
        new PersonasView(usuarioActual, personaRepository, empresaRepository,
                crearPersonaService, actualizarPersonaService, listarPersonasPorEmpresaService).mostrar();
    }

    private void abrirEmpresas() {
        new EmpresasView(usuarioActual, empresaRepository, crearEmpresaService).mostrar();
    }

    private void abrirReporteDentro() {
        new ReporteVisitasDentroView(usuarioActual, generarReporteVisitasDentroService).mostrar();
    }

    private void abrirReporteBitacora() {
        new ReporteBitacoraView(usuarioActual, generarReporteBitacoraService, usuarioRepository).mostrar();
    }

    public void mostrar() {
        setVisible(true);
    }
}
