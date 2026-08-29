package com.acme.sica.visitas.domain;

import com.acme.sica.personas.domain.Persona;
import com.acme.sica.personas.domain.TipoPersona;
import com.acme.sica.shared.ValidacionIngresoException;

/**
 * Implementaci\u00f3n Strategy para validaci\u00f3n de ingreso por carnet olvidado.
 *
 * <p>Junto con {@link ValidacionIngresoNoAnunciado} completa el patr\u00f3n
 * <b>Strategy</b> (uno de los 5 obligatorios) para validaciones de ingreso
 * según el flujo de origen.
 *
 * <p>Reglas m\u00e1s restrictivas que la validaci\u00f3n de no anunciado:
 * <ul>
 *   <li>Solo TRABAJADOR (no INVITADO).</li>
 *   <li>Nombre y documento no vac\u00edos (heredado de la validaci\u00f3n b\u00e1sica).</li>
 * </ul>
 */
public final class ValidacionIngresoPorOlvido implements ReglaValidacionIngreso {

    @Override
    public void validar(Persona persona) {
        // Validación básica: nombre y documento
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw ValidacionIngresoException.nombreOVacio();
        }
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw ValidacionIngresoException.documentoOVacio();
        }

        // Regla específica carnet olvidado: solo TRABAJADOR
        if (persona.getTipo() != TipoPersona.TRABAJADOR) {
            throw new ValidacionIngresoException(
                    "Solo los trabajadores pueden usar el flujo de carnet olvidado; " +
                    "esta persona est\u00e1 registrada como " + persona.getTipo());
        }
    }
}