package com.acme.sica.visitas.application;

import com.acme.sica.visitas.domain.Visita;

/**
 * Observer para eventos de solicitud de visita.
 *
 * <p>Parte del patr\u00f3n <b>Observer</b> (uno de los 5 obligatorios).
 * Implementado por vistas que deben reaccionar a nuevas solicitudes pendientes
 * o decisiones tomadas (aprobaci\u00f3n/rechazo).
 */
public interface VisitaObserver {

    /**
     * Notifica que hay una nueva solicitud de visita pendiente de aprobaci\u00f3n.
     *
     * @param visita la visita reci\u00e9n creada en estado PENDIENTE_APROBACION
     *               (o PENDIENTE_APROBACION_OLVIDO en HU-11)
     */
    void onSolicitudPendiente(Visita visita);

    /**
     * Notifica que se tom\u00f3 una decisi\u00f3n sobre una visita (aprobada/rechazada).
     *
     * @param visita la visita con estado actualizado (APROBADA o RECHAZADA)
     */
    void onDecisionTomada(Visita visita);
}