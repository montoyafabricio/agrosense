package com.agrosense.dto;

public record ProcessingStatusDTO(
    String estado,
    int totalAlertas,
    int alertasEnviadas,
    String mensaje
) {}
