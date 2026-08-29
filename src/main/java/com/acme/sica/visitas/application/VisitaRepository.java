package com.acme.sica.visitas.application;

import com.acme.sica.visitas.domain.Visita;

import java.util.Optional;

public interface VisitaRepository {
    Visita guardar(Visita visita);

    Visita actualizar(Visita visita);

    /**
     * Busca la visita más reciente de una persona con estado APROBADA
     * y fecha_hora_ingreso IS NULL (aprobada pero aún no usada para check-in).
     */
    Optional<Visita> buscarVisitaAprobadaPendienteDeIngreso(Long personaId);

    /**
     * Busca la visita activa (estado DENTRO) más reciente de una persona.
     * Este método no se usa en HU-07/08, pero HU-12/HU-13 lo necesitarán.
     */
    Optional<Visita> buscarVisitaActivaPorPersona(Long personaId);
}