package com.agrosense.medicion.dto;

import java.util.List;

public record MedicionProcessingResponseDTO(
    MedicionResponseDTO medicion,
    List<AlertaResponseDTO> alertasGeneradas,
    ProcessingStatusDTO procesamiento
) {}
