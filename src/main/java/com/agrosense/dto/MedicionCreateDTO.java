package com.agrosense.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicionCreateDTO(
    @NotNull(message = "La temperatura es obligatoria")
    Double temperatura,

    @NotNull(message = "La humedad es obligatoria")
    @DecimalMin(value = "0.0", message = "La humedad relativa no puede ser menor a 0%")
    @DecimalMax(value = "100.0", message = "La humedad relativa no puede ser superior a 100%")
    Double humedad,

    @NotNull(message = "La fecha y hora de la medicion es obligatoria")
    OffsetDateTime fecha,

    @NotNull(message = "El ID del sensor es obligatorio")
    UUID sensorId
) {
    public MedicionCreateDTO {
        if (temperatura != null && (temperatura.isNaN() || temperatura.isInfinite())) {
            throw new IllegalArgumentException("La temperatura debe ser un numero finito en grados Celsius");
        }
        if (humedad != null && (humedad.isNaN() || humedad.isInfinite())) {
            throw new IllegalArgumentException("La humedad debe ser un numero finito");
        }
    }
}
