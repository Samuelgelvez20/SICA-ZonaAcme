package com.acme.sica.visitas.domain;

import com.acme.sica.personas.domain.Persona;

/**
 * Interfaz Strategy para validar reglas de ingreso según el flujo de origen.
 *
 * <p>Este es el patr\u00f3n <b>Strategy</b> (uno de los 5 patrones obligatorios
 * del proyecto). Permite intercambiar la l\u00f3gica de validaci\u00f3n sin modificar
 * el servicio de aplicaci\u00f3n que la usa ({@link com.acme.sica.visitas.application.RegistrarVisitaNoAnunciadaService}).
 *
 * <p>Implementaciones previstas:
 * <ul>
 *   <li>{@link ValidacionIngresoNoAnunciado}: validaci\u00f3n b\u00e1sica para invitados no anunciados (HU-09).</li>
 *   <li>{@link com.acme.sica.visitas.domain.ValidacionIngresoPorOlvido}: validaci\u00f3n m\u00e1s restrictiva para carnet olvidado (HU-11).</li>
 * </ul>
 */
public interface ReglaValidacionIngreso {

    /**
     * Valida que la persona cumple los requisitos para el tipo de ingreso.
     *
     * @param persona persona a validar (ya existe en BD o reci\u00e9n creada)
     * @throws ValidacionIngresoException si la validaci\u00f3n falla
     */
    void validar(Persona persona);
}