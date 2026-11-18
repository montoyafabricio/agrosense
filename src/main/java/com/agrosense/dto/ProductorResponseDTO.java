package com.agrosense.dto;

import com.agrosense.model.Productor;
import java.util.UUID;

public record ProductorResponseDTO(
    UUID id,
    String nombre,
    String contacto
) {
    public static ProductorResponseDTO fromEntity(Productor productor) {
        return new ProductorResponseDTO(
            productor.getId(),
            productor.getNombre(),
            productor.getContacto()
        );
    }
}
