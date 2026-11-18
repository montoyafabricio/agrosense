package com.agrosense.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SensorEstado {
    ACTIVO("activo"),
    INACTIVO("inactivo"),
    MANTENIMIENTO("mantenimiento");

    private final String value;

    SensorEstado(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SensorEstado fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SensorEstado estado : values()) {
            if (estado.value.equalsIgnoreCase(value.trim())) {
                return estado;
            }
        }
        throw new IllegalArgumentException("El estado del sensor debe ser 'activo', 'inactivo' o 'mantenimiento'");
    }
}
