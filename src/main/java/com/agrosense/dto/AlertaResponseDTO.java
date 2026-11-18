package com.agrosense.dto;

import com.agrosense.model.Alerta;
import com.agrosense.model.AlertaPrioridad;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaResponseDTO(
    UUID id,
    String tipo,
    String descripcion,
    AlertaPrioridad prioridad,
    OffsetDateTime fecha,
    UUID cultivoId
) {
    public static AlertaResponseDTO fromEntity(Alerta alerta) {
        return new AlertaResponseDTO(
            alerta.getId(),
            alerta.getTipo(),
            alerta.getDescripcion(),
            alerta.getPrioridad(),
            alerta.getFecha(),
            alerta.getCultivoId()
        );
    }
}
