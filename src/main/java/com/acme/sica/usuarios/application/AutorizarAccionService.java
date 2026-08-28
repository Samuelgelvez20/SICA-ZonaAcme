package com.acme.sica.usuarios.application;

import com.acme.sica.shared.AccesoDenegadoException;
import com.acme.sica.usuarios.domain.Usuario;

public class AutorizarAccionService {

    /**
     * Verifica que el usuario autenticado tenga el permiso requerido.
     *
     * <p>No consulta la base de datos: opera sobre el {@link Usuario} ya
     * cargado en memoria por {@code AutenticarUsuarioService} al iniciar
     * la sesion. Esto es deliberado: los permisos del usuario no cambian
     * durante la sesion, asi que una consulta a BD por cada accion seria
     * un costo innecesario. Si en el futuro hace falta revocacion
     * inmediata, se invalida la sesion y se re-autentica.
     *
     * @param usuario           usuario autenticado (puede ser null por seguridad)
     * @param permisoRequerido  codigo del permiso exigido por el caso de uso
     * @throws AccesoDenegadoException si el usuario es null, esta inactivo,
     *                                  o carece del permiso requerido
     */
    public void verificar(Usuario usuario, String permisoRequerido) {
        if (permisoRequerido == null || permisoRequerido.isBlank()) {
            // Programacion defensiva: un caso de uso nunca debe llamar con
            // un permiso nulo o vacio, pero si pasa lo tratamos como denegado
            // en vez de devolver un NullPointerException que filtre informacion.
            throw AccesoDenegadoException.porPermisoFaltante(
                    usuario == null ? "<sin usuario>" : usuario.getUsername(),
                    String.valueOf(permisoRequerido));
        }

        if (usuario == null || !usuario.isActivo()) {
            // Defensa en profundidad: el login ya valida activo, pero si
            // un caso de uso recibio un usuario inactivo por otro camino,
            // volvemos a denegar.
            throw AccesoDenegadoException.porPermisoFaltante(
                    usuario == null ? "<sin usuario>" : usuario.getUsername(),
                    permisoRequerido);
        }

        if (!usuario.getRol().tienePermiso(permisoRequerido)) {
            // TODO HU-05: registrar AUTORIZACION_DENEGADA en bitacora_auditoria
            //             con detalle = "username=" + usuario.getUsername()
            //                       + " permiso=" + permisoRequerido.
            //             Las verificaciones exitosas NO se auditan aqui;
            //             las audita el caso de uso de negocio que origino
            //             la llamada.
            throw AccesoDenegadoException.porPermisoFaltante(
                    usuario.getUsername(), permisoRequerido);
        }

        // Permiso concedido: no-op. La accion se audita en su propio servicio.
    }
}
