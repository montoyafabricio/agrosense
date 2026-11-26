package com.agrosense.dto;

import com.agrosense.model.AlertaPrioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaCreateDTO(
    @NotBlank(message = "El tipo de alerta es obligatorio")
    String tipo,

    @NotBlank(message = "La descripcion es obligatoria")
    String descripcion,

    @NotNull(message = "La prioridad de la alerta es obligatoria")
    AlertaPrioridad prioridad,

    @NotNull(message = "La fecha de la alerta es obligatoria")
    OffsetDateTime fecha,

    @NotNull(message = "El ID del cultivo es obligatorio")
    UUID cultivoId
) {
    public AlertaCreateDTO {
        if (tipo != null) {
            tipo = tipo.trim();
        }
        if (descripcion != null) {
            descripcion = descripcion.trim();
        }
    }
}
