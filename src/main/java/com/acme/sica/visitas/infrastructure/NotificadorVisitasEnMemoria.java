package com.acme.sica.visitas.infrastructure;

import com.acme.sica.visitas.application.NotificadorVisitas;
import com.acme.sica.visitas.application.VisitaObserver;
import com.acme.sica.visitas.domain.Visita;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementaci\u00f3n en memoria del notificador de visitas (Patr\u00f3n Observer).
 *
 * <p>Este es el patr\u00f3n <b>Observer</b> (uno de los 5 patrones obligatorios
 * del proyecto). Mantiene una lista thread-safe de observers y despacha
 * notificaciones en hilos separados para no bloquear al emisor.
 *
 * <p>Usa un {@code ExecutorService} con pool fijo de 2 hilos:
 * <ul>
 *   <li>Hilo 1: despacha notificaciones de solicitudes pendientes</li>
 *   <li>Hilo 2: despacha notificaciones de decisiones tomadas</li>
 * </ul>
 * Tama\u00f1o 2 es suficiente porque las notificaciones son eventos discretos
 * de baja frecuencia (no streaming de alta throughput) y queremos que
 * una notificaci\u00f3n lenta no bloquee a la otra categor\u00eda.
 *
 * <p>Si un observer lanza excepci\u00f3n, se captura y loguea sin afectar
 * a los dem\u00e1s observers ni tumbar el hilo de despacho.
 */
public final class NotificadorVisitasEnMemoria implements NotificadorVisitas {

    private static final Logger LOG = Logger.getLogger(NotificadorVisitasEnMemoria.class.getName());

    private final CopyOnWriteArrayList<VisitaObserver> observers = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void suscribir(VisitaObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void desuscribir(VisitaObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notificarSolicitudPendiente(Visita visita) {
        if (visita == null) {
            return;
        }
        executor.submit(() -> {
            for (VisitaObserver observer : observers) {
                try {
                    observer.onSolicitudPendiente(visita);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Observer fall\u00f3 en onSolicitudPendiente: " + observer.getClass().getName(), e);
                }
            }
        });
    }

    @Override
    public void notificarDecisionTomada(Visita visita) {
        if (visita == null) {
            return;
        }
        executor.submit(() -> {
            for (VisitaObserver observer : observers) {
                try {
                    observer.onDecisionTomada(visita);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Observer fall\u00f3 en onDecisionTomada: " + observer.getClass().getName(), e);
                }
            }
        });
    }

    /**
     * Cierra el executor service (para pruebas o apagado ordenado).
     */
    public void shutdown() {
        executor.shutdown();
    }
}