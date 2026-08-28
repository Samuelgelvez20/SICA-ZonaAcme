package com.acme.sica.auditoria.application;

import com.acme.sica.auditoria.domain.BitacoraAuditoria;

public interface BitacoraRepository {
    void guardar(BitacoraAuditoria registro);
}