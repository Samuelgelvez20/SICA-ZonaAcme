package com.acme.sica.auditoria.application;

import com.acme.sica.auditoria.domain.BitacoraAuditoria;
import com.acme.sica.auditoria.domain.ResultadoAuditoria;

import java.time.LocalDateTime;

public class AuditoriaService {

    private final BitacoraRepository bitacoraRepository;

    public AuditoriaService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public void registrar(Long usuarioId, String accion, String entidad,
                          String detalle, ResultadoAuditoria resultado) {
        BitacoraAuditoria registro = new BitacoraAuditoria(
                usuarioId, accion, entidad, detalle, LocalDateTime.now(), resultado
        );
        bitacoraRepository.guardar(registro);
    }
}