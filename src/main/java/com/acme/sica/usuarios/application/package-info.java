/**
 * Servicios del slice de usuarios (puertos y casos de uso).
 *
 * <p>Convencion del proyecto: todo caso de uso de negocio que requiera
 * un permiso especifico debe invocar
 * {@code AutorizarAccionService.verificar(usuarioAutenticado, codigoPermiso)}
 * como primera linea de su logica, ANTES de tocar el dominio o la
 * infraestructura. Si el usuario carece del permiso, se lanza
 * {@link com.acme.sica.shared.AccesoDenegadoException} y el caso de uso
 * nunca llega a ejecutarse.
 *
 * <p>Auditoria: las verificaciones EXITOSAS no se registran aqui; lo hara
 * el caso de uso de negocio que origino la llamada (ej: "registrar_visita
 * EXITO" en su propio servicio). Las DENEGACIONES si se registran aqui
 * mismo (HU-05), porque no hay un servicio de negocio que las reporte.
 */
package com.acme.sica.usuarios.application;
