package com.agrosense.alerta.dto;

import com.agrosense.alerta.model.Alerta;
import com.agrosense.alerta.model.AlertaPrioridad;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaResponseDTO(
    UUID id,
    String tipo,
    String descripcion,
    AlertaPrioridad prioridad,
    OffsetDateTime fecha,
    UUID cultivoId,
    OffsetDateTime createdAt
) {
    public static AlertaResponseDTO fromEntity(Alerta a) {
        return new AlertaResponseDTO(
            a.getId(), a.getTipo(), a.getDescripcion(),
            a.getPrioridad(), a.getFecha(), a.getCultivoId(), a.getCreatedAt()
        );
    }
}
