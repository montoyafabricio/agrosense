package com.agrosense.dto;

import com.agrosense.model.Sensor;
import com.agrosense.model.SensorEstado;
import com.agrosense.model.SensorTipo;
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
