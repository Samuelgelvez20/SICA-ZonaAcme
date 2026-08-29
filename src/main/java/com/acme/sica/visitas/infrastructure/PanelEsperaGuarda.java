package com.acme.sica.visitas.infrastructure;

import com.acme.sica.personas.application.PersonaRepository;
import com.acme.sica.personas.domain.Persona;
import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.RegistrarCheckInService;
import com.acme.sica.visitas.application.VisitaObserver;
import com.acme.sica.visitas.domain.Visita;
import com.acme.sica.usuarios.domain.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Panel ligero para el Guarda que muestra notificaciones de decisiones
 * sobre sus solicitudes registradas.
 *
 * <p>Implementa {@link VisitaObserver} para recibir notificaciones
 * cuando un funcionario aprueba o rechaza una visita que el guarda registró.
 */
public class PanelEsperaGuarda extends JPanel implements VisitaObserver {

    private final Usuario guardaActual;
    private final NotificadorVisitas notificadorVisitas;
    private final RegistrarCheckInService registrarCheckInService;
    private final PersonaRepository personaRepository;
    private final JLabel statusLabel;

    public PanelEsperaGuarda(Usuario guardaActual,
                             NotificadorVisitas notificadorVisitas,
                             RegistrarCheckInService registrarCheckInService,
                             PersonaRepository personaRepository) {
        this.guardaActual = guardaActual;
        this.notificadorVisitas = notificadorVisitas;
        this.registrarCheckInService = registrarCheckInService;
        this.personaRepository = personaRepository;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Panel de Espera - " + guardaActual.getNombre()));

        statusLabel = new JLabel("Esperando decisiones...", SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(statusLabel, BorderLayout.CENTER);

        notificadorVisitas.suscribir(this);
    }

    @Override
    public void onSolicitudPendiente(Visita visita) {
        // El guarda no necesita ver nuevas solicitudes de otros guardas
        // Solo le interesan las decisiones sobre SUS solicitudes
    }

    @Override
    public void onDecisionTomada(Visita visita) {
        // Solo reaccionar si la visita fue registrada por ESTE guarda
        if (visita.getGuardaId() != null && visita.getGuardaId().equals(guardaActual.getId())) {
            SwingUtilities.invokeLater(() -> mostrarDecision(visita));
        }
    }

    private void mostrarDecision(Visita visita) {
        String mensaje;
        int tipoMensaje;

        if (visita.getEstado().name().equals("APROBADA")) {
            mensaje = "La solicitud de " + obtenerNombrePersona(visita) + " fue APROBADA.\n\n" +
                    "¿Desea realizar el check-in ahora?";
            tipoMensaje = JOptionPane.INFORMATION_MESSAGE;
        } else if (visita.getEstado().name().equals("RECHAZADA")) {
            mensaje = "La solicitud de " + obtenerNombrePersona(visita) + " fue RECHAZADA.\n\n" +
                    "Motivo: " + (visita.getMotivo() != null ? visita.getMotivo() : "—");
            tipoMensaje = JOptionPane.WARNING_MESSAGE;
        } else {
            return; // otros estados no son decisiones finales
        }

        statusLabel.setText("Última decisión: " + visita.getEstado());

        if (visita.getEstado().name().equals("APROBADA")) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    mensaje,
                    "Decisión recibida",
                    JOptionPane.YES_NO_OPTION,
                    tipoMensaje
            );
            if (opcion == JOptionPane.YES_OPTION) {
                hacerCheckInInmediato(visita);
            }
        } else {
            JOptionPane.showMessageDialog(this, mensaje, "Decisión recibida", tipoMensaje);
        }
    }

    private String obtenerNombrePersona(Visita visita) {
        return personaRepository.buscarPorId(visita.getPersonaId())
                .map(Persona::getNombre)
                .orElse("Persona ID " + visita.getPersonaId());
    }

    private void hacerCheckInInmediato(Visita visita) {
        try {
            var personaOpt = personaRepository.buscarPorId(visita.getPersonaId());
            if (personaOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró la persona (ID " + visita.getPersonaId() + ") en el sistema.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String documento = personaOpt.get().getDocumento();
            registrarCheckInService.ejecutar(guardaActual, documento);
            JOptionPane.showMessageDialog(this, "Check-in realizado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en check-in: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cerrar() {
        notificadorVisitas.desuscribir(this);
    }
}