package com.agrosense.medicion.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para crear alertas en el alerta-service via HTTP.
 */
public record AlertaCreateDTO(
    String tipo,
    String descripcion,
    String prioridad,
    OffsetDateTime fecha,
    UUID cultivoId
) {}
