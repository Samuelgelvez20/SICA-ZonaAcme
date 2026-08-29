package com.acme.sica.shared;

public class VisitaNoDecidibleException extends RuntimeException {

    public VisitaNoDecidibleException(String mensaje) {
        super(mensaje);
    }

    public static VisitaNoDecidibleException estadoNoValido(Long visitaId, String estadoActual) {
        return new VisitaNoDecidibleException(
                "La visita " + visitaId + " no est\u00e1 en estado decidible (actual: " + estadoActual + ")");
    }

    public static VisitaNoDecidibleException yaDecidida(Long visitaId, String estadoActual) {
        return new VisitaNoDecidibleException(
                "La visita " + visitaId + " ya fue decidida (estado: " + estadoActual + ")");
    }
}