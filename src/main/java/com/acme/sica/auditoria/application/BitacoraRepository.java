package com.acme.sica.auditoria.application;

import com.acme.sica.auditoria.domain.BitacoraAuditoria;

import java.time.LocalDateTime;
import java.util.List;

public interface BitacoraRepository {
    void guardar(BitacoraAuditoria registro);

    List<BitacoraAuditoria> listarConFiltros(Long usuarioId, String accion,
                                              LocalDateTime fechaDesde, LocalDateTime fechaHasta);
}