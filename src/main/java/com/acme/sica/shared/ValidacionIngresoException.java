package com.acme.sica.shared;

public class ValidacionIngresoException extends RuntimeException {

    public ValidacionIngresoException(String mensaje) {
        super(mensaje);
    }

    public static ValidacionIngresoException nombreOVacio() {
        return new ValidacionIngresoException("El nombre de la persona es obligatorio");
    }

    public static ValidacionIngresoException documentoOVacio() {
        return new ValidacionIngresoException("El documento de la persona es obligatorio");
    }
}