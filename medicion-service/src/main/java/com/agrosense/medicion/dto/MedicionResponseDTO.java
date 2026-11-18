package com.agrosense.medicion.dto;

import com.agrosense.medicion.model.Medicion;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicionResponseDTO(
    UUID id,
    Double temperatura,
    Double humedad,
    OffsetDateTime fecha,
    UUID sensorId,
    OffsetDateTime createdAt
) {
    public static MedicionResponseDTO fromEntity(Medicion m) {
        return new MedicionResponseDTO(
            m.getId(), m.getTemperatura(), m.getHumedad(),
            m.getFecha(), m.getSensorId(), m.getCreatedAt()
        );
    }
}
