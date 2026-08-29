package com.acme.sica.visitas.application;

import com.acme.sica.visitas.domain.Visita;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository {
    Visita guardar(Visita visita);

    Visita actualizar(Visita visita);

    /**
     * Busca la visita m\u00e1s reciente de una persona con estado APROBADA
     * y fecha_hora_ingreso IS NULL (aprobada pero a\u00fan no usada para check-in).
     */
    Optional<Visita> buscarVisitaAprobadaPendienteDeIngreso(Long personaId);

    /**
     * Busca la visita activa (estado DENTRO) m\u00e1s reciente de una persona.
     * Este m\u00e9todo no se usa en HU-07/08, pero HU-12/HU-13 lo necesitar\u00e1n.
     */
    Optional<Visita> buscarVisitaActivaPorPersona(Long personaId);

    /**
     * Busca una visita por su ID.
     */
    Optional<Visita> buscarPorId(Long id);

    /**
     * Lista visitas pendientes de aprobaci\u00f3n (PENDIENTE_APROBACION o
     * PENDIENTE_APROBACION_OLVIDO) para un funcionario dado.
     * \u00datil para cargar el estado inicial de la pantalla del funcionario.
     */
    List<Visita> listarPendientesPorFuncionario(Long funcionarioId);
}