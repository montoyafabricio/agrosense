package com.agrosense.alerta.model;

public enum AlertaPrioridad {
    BAJA("baja"),
    MEDIA("media"),
    ALTA("alta");

    private final String value;

    AlertaPrioridad(String value) { this.value = value; }
    public String getValue() { return value; }
}
