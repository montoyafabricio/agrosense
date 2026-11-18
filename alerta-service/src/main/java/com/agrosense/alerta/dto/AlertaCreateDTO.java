package com.agrosense.alerta.dto;

import com.agrosense.alerta.model.AlertaPrioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaCreateDTO(
    @NotBlank String tipo,
    @NotBlank String descripcion,
    @NotNull AlertaPrioridad prioridad,
    @NotNull OffsetDateTime fecha,
    @NotNull UUID cultivoId
) {}
