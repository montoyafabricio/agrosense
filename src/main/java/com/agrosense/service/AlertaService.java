package com.agrosense.service;

import com.agrosense.dto.AlertaCreateDTO;
import com.agrosense.dto.AlertaResponseDTO;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Alerta;
import com.agrosense.model.AlertaPrioridad;
import com.agrosense.repository.AlertaRepository;
import com.agrosense.repository.CultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final CultivoRepository cultivoRepository;

    public AlertaService(AlertaRepository alertaRepository, CultivoRepository cultivoRepository) {
        this.alertaRepository = alertaRepository;
        this.cultivoRepository = cultivoRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> getAll(UUID cultivoId, AlertaPrioridad prioridad) {
        List<Alerta> list;
        if (cultivoId != null && prioridad != null) {
            list = alertaRepository.findByCultivoIdAndPrioridadOrderByFechaDesc(cultivoId, prioridad);
        } else if (cultivoId != null) {
            list = alertaRepository.findByCultivoIdOrderByFechaDesc(cultivoId);
        } else if (prioridad != null) {
            list = alertaRepository.findByPrioridadOrderByFechaDesc(prioridad);
        } else {
            list = alertaRepository.findAllByOrderByFechaDesc();
        }

        return list.stream()
            .map(AlertaResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponseDTO getById(UUID id) {
        Alerta alerta = alertaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerta con ID " + id + " no encontrada"));
        return AlertaResponseDTO.fromEntity(alerta);
    }

    public AlertaResponseDTO create(AlertaCreateDTO dto) {
        if (!cultivoRepository.existsById(dto.cultivoId())) {
            throw new ResourceNotFoundException("No se puede crear la alerta: El Cultivo con ID " + dto.cultivoId() + " no existe");
        }

        // Control de Idempotencia: ventana de 120 segundos
        OffsetDateTime fromTime = dto.fecha().minusSeconds(120);
        OffsetDateTime toTime = dto.fecha().plusSeconds(120);
        List<Alerta> duplicates = alertaRepository.findDuplicates(dto.cultivoId(), dto.tipo(), fromTime, toTime);
        if (!duplicates.isEmpty()) {
            return AlertaResponseDTO.fromEntity(duplicates.getFirst());
        }

        Alerta nueva = new Alerta(null, dto.tipo(), dto.descripcion(), dto.prioridad(), dto.fecha(), dto.cultivoId());
        Alerta guardada = alertaRepository.save(nueva);
        return AlertaResponseDTO.fromEntity(guardada);
    }
}
