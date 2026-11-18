package com.agrosense.sensor;

import java.util.UUID;

public record SensorResponseDTO(
    UUID id,
    SensorTipo tipo,
    String ubicacion,
    SensorEstado estado,
    UUID cultivoId
) {
    public static SensorResponseDTO fromEntity(Sensor sensor) {
        return new SensorResponseDTO(
            sensor.getId(),
            sensor.getTipo(),
            sensor.getUbicacion(),
            sensor.getEstado(),
            sensor.getCultivoId()
        );
    }
}
