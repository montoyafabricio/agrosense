package com.agrosense.dto;

import com.agrosense.model.SensorEstado;
import com.agrosense.model.SensorTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SensorCreateDTO(
    @NotNull(message = "El tipo de sensor es obligatorio")
    SensorTipo tipo,

    @NotBlank(message = "La ubicacion es obligatoria")
    String ubicacion,

    @NotNull(message = "El estado del sensor es obligatorio")
    SensorEstado estado,

    @NotNull(message = "El ID del cultivo es obligatorio")
    UUID cultivoId
) {
    public SensorCreateDTO {
        if (ubicacion != null) {
            ubicacion = ubicacion.trim();
        }
    }
}
