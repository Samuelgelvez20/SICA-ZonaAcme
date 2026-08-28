package com.acme.sica.shared;

public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public static AccesoDenegadoException porPermisoFaltante(String username, String codigoPermiso) {
        return new AccesoDenegadoException(
                "El usuario " + username + " no tiene el permiso requerido: " + codigoPermiso);
    }
}
