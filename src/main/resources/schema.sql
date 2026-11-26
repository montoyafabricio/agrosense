-- ==========================================================
-- AGROSENSE - SCRIPT DE INICIALIZACION DE BASE DE DATOS
-- Arquitectura de 4 Esquemas para PostgreSQL
-- ==========================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. ESQUEMA GESTION
CREATE SCHEMA IF NOT EXISTS gestion;

CREATE TABLE IF NOT EXISTS gestion.productores (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL CHECK (length(trim(nombre)) > 0),
    contacto VARCHAR(255) NOT NULL CHECK (length(trim(contacto)) > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gestion.cultivos (
    id UUID PRIMARY KEY,
    especie VARCHAR(255) NOT NULL CHECK (length(trim(especie)) > 0),
    superficie DOUBLE PRECISION NOT NULL CHECK (superficie > 0),
    fecha_siembra DATE NOT NULL,
    productor_id UUID NOT NULL REFERENCES gestion.productores(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cultivos_productor_id ON gestion.cultivos(productor_id);


-- 2. ESQUEMA SENSORES
CREATE SCHEMA IF NOT EXISTS sensores;

CREATE TABLE IF NOT EXISTS sensores.sensores (
    id UUID PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('humedad', 'temperatura', 'mixto')),
    ubicacion VARCHAR(255) NOT NULL CHECK (length(trim(ubicacion)) > 0),
    estado VARCHAR(50) NOT NULL CHECK (estado IN ('activo', 'inactivo', 'mantenimiento')),
    cultivo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sensores_cultivo_id ON sensores.sensores(cultivo_id);
CREATE INDEX IF NOT EXISTS idx_sensores_estado ON sensores.sensores(estado);


-- 3. ESQUEMA MEDICIONES
CREATE SCHEMA IF NOT EXISTS mediciones;

CREATE TABLE IF NOT EXISTS mediciones.mediciones (
    id UUID PRIMARY KEY,
    temperatura DOUBLE PRECISION NOT NULL,
    humedad DOUBLE PRECISION NOT NULL CHECK (humedad >= 0 AND humedad <= 100),
    fecha TIMESTAMPTZ NOT NULL,
    sensor_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mediciones_sensor_id ON mediciones.mediciones(sensor_id);
CREATE INDEX IF NOT EXISTS idx_mediciones_fecha ON mediciones.mediciones(fecha DESC);

CREATE TABLE IF NOT EXISTS mediciones.mediciones_procesamiento (
    medicion_id UUID PRIMARY KEY REFERENCES mediciones.mediciones(id) ON DELETE CASCADE,
    estado VARCHAR(50) NOT NULL CHECK (estado IN ('pendiente', 'completado', 'sin_alertas')),
    alertas_generadas JSONB NOT NULL DEFAULT '[]'::jsonb,
    reintentos INT NOT NULL DEFAULT 0,
    ultimo_error TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 4. ESQUEMA ALERTAS
CREATE SCHEMA IF NOT EXISTS alertas;

CREATE TABLE IF NOT EXISTS alertas.alertas (
    id UUID PRIMARY KEY,
    tipo VARCHAR(100) NOT NULL CHECK (length(trim(tipo)) > 0),
    descripcion TEXT NOT NULL CHECK (length(trim(descripcion)) > 0),
    prioridad VARCHAR(20) NOT NULL CHECK (prioridad IN ('baja', 'media', 'alta')),
    fecha TIMESTAMPTZ NOT NULL,
    cultivo_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alertas_cultivo_id ON alertas.alertas(cultivo_id);
CREATE INDEX IF NOT EXISTS idx_alertas_prioridad ON alertas.alertas(prioridad);
CREATE INDEX IF NOT EXISTS idx_alertas_fecha ON alertas.alertas(fecha DESC);
