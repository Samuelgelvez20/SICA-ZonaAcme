package com.acme.sica.shared;

public class PersonaBloqueadaException extends RuntimeException {

    public PersonaBloqueadaException(String mensaje) {
        super(mensaje);
    }

    public static PersonaBloqueadaException conMotivo(String documento, String motivo) {
        return new PersonaBloqueadaException(
                "La persona con documento " + documento +
                " est\u00e1 bloqueada. Motivo: " + motivo);
    }
}
