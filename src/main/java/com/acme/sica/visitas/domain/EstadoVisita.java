package com.acme.sica.visitas.domain;

/**
 * Estados posibles de una visita según el CHECK constraint de la tabla visitas.
 * Los 7 valores exactos deben coincidir con schema.sql.
 */
public enum EstadoVisita {
    APROBADA,
    PENDIENTE_APROBACION,
    PENDIENTE_APROBACION_OLVIDO,
    DENTRO,
    RECHAZADA,
    CERRADA,
    CERRADA_POR_SISTEMA_SALIDA_OLVIDADA
}