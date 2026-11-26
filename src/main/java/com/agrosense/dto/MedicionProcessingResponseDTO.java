package com.agrosense.dto;

import java.util.List;

public record MedicionProcessingResponseDTO(
    MedicionResponseDTO medicion,
    List<AlertaResponseDTO> alertas,
    ProcessingStatusDTO procesamiento
) {}
