package com.agrosense.medicion.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicionCreateDTO(
    @NotNull Double temperatura,
    @NotNull Double humedad,
    @NotNull OffsetDateTime fecha,
    @NotNull UUID sensorId
) {}
