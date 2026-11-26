package com.agrosense.dto;

import com.agrosense.model.Medicion;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicionResponseDTO(
    UUID id,
    Double temperatura,
    Double humedad,
    OffsetDateTime fecha,
    UUID sensorId
) {
    public static MedicionResponseDTO fromEntity(Medicion medicion) {
        return new MedicionResponseDTO(
            medicion.getId(),
            medicion.getTemperatura(),
            medicion.getHumedad(),
            medicion.getFecha(),
            medicion.getSensorId()
        );
    }
}
