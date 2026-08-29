package com.acme.sica.shared;

public class SinVisitaAprobadaException extends RuntimeException {

    public SinVisitaAprobadaException(String mensaje) {
        super(mensaje);
    }

    public static SinVisitaAprobadaException sinVisitaPendiente(String documento) {
        return new SinVisitaAprobadaException(
                "Esta persona no tiene ninguna visita aprobada pendiente de ingreso."
        );
    }

    public static SinVisitaAprobadaException porDocumento(String documento) {
        return new SinVisitaAprobadaException(
                "La persona con documento " + documento + " no tiene ninguna visita aprobada pendiente de ingreso."
        );
    }
}