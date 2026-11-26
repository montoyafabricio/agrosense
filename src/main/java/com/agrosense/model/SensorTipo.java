package com.agrosense.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SensorTipo {
    HUMEDAD("humedad"),
    TEMPERATURA("temperatura"),
    MIXTO("mixto");

    private final String value;

    SensorTipo(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SensorTipo fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SensorTipo tipo : values()) {
            if (tipo.value.equalsIgnoreCase(value.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("El tipo de sensor debe ser 'humedad', 'temperatura' o 'mixto'");
    }
}
