package com.agrosense.medicion.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO simplificado de AlertaResponseDTO para el medicion-service.
 * Se usa para representar las alertas creadas en el alerta-service
 * via llamada HTTP REST.
 */
public record AlertaResponseDTO(
    UUID id,
    String tipo,
    String descripcion,
    String prioridad,
    OffsetDateTime fecha,
    UUID cultivoId,
    OffsetDateTime createdAt
) {}
