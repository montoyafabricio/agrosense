package com.agrosense.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertaPrioridad {
    BAJA("baja"),
    MEDIA("media"),
    ALTA("alta");

    private final String value;

    AlertaPrioridad(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AlertaPrioridad fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AlertaPrioridad prioridad : values()) {
            if (prioridad.value.equalsIgnoreCase(value.trim())) {
                return prioridad;
            }
        }
        throw new IllegalArgumentException("La prioridad de la alerta debe ser 'baja', 'media' o 'alta'");
    }
}
