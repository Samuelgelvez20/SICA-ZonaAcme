package com.acme.sica.incidentes.application;

import com.acme.sica.incidentes.domain.Incidente;

public interface IncidenteRepository {
    Incidente guardar(Incidente incidente);
}
