package com.acme.sica.shared;

public class PersonaNoEncontradaException extends RuntimeException {

    public PersonaNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public static PersonaNoEncontradaException porDocumento(String documento) {
        return new PersonaNoEncontradaException(
                "No existe ninguna persona registrada con documento " + documento +
                ". Debe registrarse primero (flujo disponible próximamente)."
        );
    }

    public static PersonaNoEncontradaException porId(Long id) {
        return new PersonaNoEncontradaException(
                "No existe ninguna persona registrada con ID " + id + "."
        );
    }
}