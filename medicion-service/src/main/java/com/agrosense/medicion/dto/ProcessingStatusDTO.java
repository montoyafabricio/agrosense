package com.agrosense.medicion.dto;

public record ProcessingStatusDTO(
    String estado,
    int alertasDetectadas,
    int alertasCreadas,
    String mensaje
) {}
