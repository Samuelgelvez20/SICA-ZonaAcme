package com.acme.sica.visitas.domain;

import com.acme.sica.personas.domain.Persona;
import com.acme.sica.shared.ValidacionIngresoException;

/**
 * Implementaci\u00f3n Strategy para validaci\u00f3n de ingreso de invitado no anunciado.
 *
 * <p>Validaci\u00f3n liviana a prop\u00f3sito: solo verifica que nombre y documento
 * no sean null ni vac\u00edos. HU-11 agregar\u00e1 {@link ValidacionIngresoPorOlvido}
 * con reglas m\u00e1s estrictas (solo TRABAJADOR, empresa obligatoria, etc.),
 * demostrando el valor del patr\u00f3n Strategy.
 */
public final class ValidacionIngresoNoAnunciado implements ReglaValidacionIngreso {

    @Override
    public void validar(Persona persona) {
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw ValidacionIngresoException.nombreOVacio();
        }
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw ValidacionIngresoException.documentoOVacio();
        }
    }
}