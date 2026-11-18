package com.agrosense.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SensorUpdateDTO(
    @NotNull(message = "El tipo de sensor es obligatorio")
    SensorTipo tipo,

    @NotBlank(message = "La ubicacion es obligatoria")
    String ubicacion,

    @NotNull(message = "El estado del sensor es obligatorio")
    SensorEstado estado,

    @NotNull(message = "El ID del cultivo es obligatorio")
    UUID cultivoId
) {
    public SensorUpdateDTO {
        if (ubicacion != null) {
            ubicacion = ubicacion.trim();
        }
    }
}
