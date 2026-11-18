package com.agrosense.gestion;

import java.util.UUID;

public record CultivoResponseDTO(
    UUID id,
    String especie,
    Double superficie,
    String fechaSiembra,
    UUID productorId
) {
    public static CultivoResponseDTO fromEntity(Cultivo cultivo) {
        return new CultivoResponseDTO(
            cultivo.getId(),
            cultivo.getEspecie(),
            cultivo.getSuperficie(),
            cultivo.getFechaSiembra() != null ? cultivo.getFechaSiembra().toString() : null,
            cultivo.getProductorId()
        );
    }
}
