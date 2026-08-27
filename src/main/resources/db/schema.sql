-- ============================================================
-- SICA - Sistema Integrado de Control de Acceso para Zona Acme
-- schema.sql: creación de todas las tablas
-- ============================================================

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permisos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL
);

CREATE TABLE rol_permisos (
    rol_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id INTEGER NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    rol_id INTEGER NOT NULL REFERENCES roles(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE empresas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE personas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TRABAJADOR', 'INVITADO')),
    foto_url VARCHAR(500),
    empresa_id INTEGER REFERENCES empresas(id),
    funcionario_anfitrion_id INTEGER REFERENCES usuarios(id),
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_bloqueo VARCHAR(255)
);

CREATE TABLE visitas (
    id SERIAL PRIMARY KEY,
    persona_id INTEGER NOT NULL REFERENCES personas(id),
    guarda_id INTEGER REFERENCES usuarios(id),
    funcionario_id INTEGER REFERENCES usuarios(id),
    empresa_visitada_id INTEGER REFERENCES empresas(id),
    fecha_hora_ingreso TIMESTAMP,
    fecha_hora_salida TIMESTAMP,
    estado VARCHAR(40) NOT NULL CHECK (estado IN (
        'APROBADA',
        'PENDIENTE_APROBACION',
        'PENDIENTE_APROBACION_OLVIDO',
        'DENTRO',
        'RECHAZADA',
        'CERRADA',
        'CERRADA_POR_SISTEMA_SALIDA_OLVIDADA'
    )),
    motivo VARCHAR(255),
    creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE incidentes (
    id SERIAL PRIMARY KEY,
    persona_id INTEGER REFERENCES personas(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE bitacora_auditoria (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuarios(id),
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(100),
    detalle VARCHAR(1000),
    fecha_hora TIMESTAMP NOT NULL DEFAULT NOW(),
    resultado VARCHAR(20) NOT NULL CHECK (resultado IN ('EXITO', 'FALLO'))
);

CREATE INDEX idx_visitas_persona ON visitas(persona_id);
CREATE INDEX idx_visitas_estado ON visitas(estado);
CREATE INDEX idx_bitacora_usuario ON bitacora_auditoria(usuario_id);
CREATE INDEX idx_bitacora_fecha ON bitacora_auditoria(fecha_hora);