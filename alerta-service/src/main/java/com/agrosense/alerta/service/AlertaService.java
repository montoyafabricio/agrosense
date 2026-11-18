package com.agrosense.alerta.service;

import com.agrosense.alerta.dto.AlertaCreateDTO;
import com.agrosense.alerta.dto.AlertaResponseDTO;
import com.agrosense.alerta.exception.ResourceNotFoundException;
import com.agrosense.alerta.model.Alerta;
import com.agrosense.alerta.model.AlertaPrioridad;
import com.agrosense.alerta.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final RestTemplate restTemplate;

    @Value("${gestion.service.url}")
    private String gestionServiceUrl;

    public AlertaService(AlertaRepository alertaRepository, RestTemplate restTemplate) {
        this.alertaRepository = alertaRepository;
        this.restTemplate = restTemplate;
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
        return list.stream().map(AlertaResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponseDTO getById(UUID id) {
        return alertaRepository.findById(id)
            .map(AlertaResponseDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Alerta con ID " + id + " no encontrada"));
    }

    public AlertaResponseDTO create(AlertaCreateDTO dto) {
        // Validar que el cultivo existe via HTTP al gestion-service
        validarCultivoExiste(dto.cultivoId());

        // Control de idempotencia: ventana de 120 segundos
        OffsetDateTime desde = dto.fecha().minusSeconds(120);
        OffsetDateTime hasta = dto.fecha().plusSeconds(120);
        List<Alerta> duplicados = alertaRepository.findDuplicates(dto.cultivoId(), dto.tipo(), desde, hasta);
        if (!duplicados.isEmpty()) {
            return AlertaResponseDTO.fromEntity(duplicados.getFirst());
        }

        Alerta nueva = new Alerta(null, dto.tipo(), dto.descripcion(), dto.prioridad(), dto.fecha(), dto.cultivoId());
        return AlertaResponseDTO.fromEntity(alertaRepository.save(nueva));
    }

    private void validarCultivoExiste(UUID cultivoId) {
        try {
            restTemplate.getForEntity(gestionServiceUrl + "/api/cultivos/" + cultivoId, Object.class);
        } catch (Exception e) {
            throw new ResourceNotFoundException("No se puede crear la alerta: El Cultivo con ID " + cultivoId + " no existe");
        }
    }
}
