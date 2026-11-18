package com.agrosense.gestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public record CultivoCreateDTO(
    @NotBlank(message = "La especie es obligatoria")
    String especie,

    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser un numero positivo mayor a 0 (hectareas)")
    Double superficie,

    @NotNull(message = "La fecha de siembra es obligatoria")
    LocalDate fechaSiembra,

    @NotNull(message = "El ID del productor es obligatorio")
    UUID productorId
) {
    public CultivoCreateDTO {
        if (especie != null) {
            especie = especie.trim();
        }
    }
}
