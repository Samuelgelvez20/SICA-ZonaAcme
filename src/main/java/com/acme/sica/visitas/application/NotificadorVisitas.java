package com.acme.sica.visitas.application;

import com.acme.sica.visitas.domain.Visita;

/**
 * Puerto para notificar eventos de visita a observers suscritos.
 *
 * <p>Parte del patr\u00f3n <b>Observer</b> (uno de los 5 obligatorios).
 * La capa de aplicaci\u00f3n define este contrato; la infraestructura provee
 * la implementaci\u00f3n concreta (en memoria, WebSocket, etc.).
 */
public interface NotificadorVisitas {

    /**
     * Suscribe un observer para recibir notificaciones.
     *
     * @param observer observer a suscribir
     */
    void suscribir(VisitaObserver observer);

    /**
     * Desuscribe un observer.
     *
     * @param observer observer a desuscribir
     */
    void desuscribir(VisitaObserver observer);

    /**
     * Notifica a todos los observers que hay una nueva solicitud pendiente.
     * El m\u00e9todo retorna inmediatamente (despacho as\u00edncrono).
     *
     * @param visita la visita reci\u00e9n creada
     */
    void notificarSolicitudPendiente(Visita visita);

    /**
     * Notifica a todos los observers que se tom\u00f3 una decisi\u00f3n.
     * El m\u00e9todo retorna inmediatamente (despacho as\u00edncrono).
     *
     * @param visita la visita con estado actualizado
     */
    void notificarDecisionTomada(Visita visita);
}