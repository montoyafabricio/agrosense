package com.agrosense.medicion.service;

import com.agrosense.medicion.dto.*;
import com.agrosense.medicion.exception.ConflictException;
import com.agrosense.medicion.exception.ResourceNotFoundException;
import com.agrosense.medicion.model.Medicion;
import com.agrosense.medicion.model.MedicionProcesamiento;
import com.agrosense.medicion.repository.MedicionProcesamientoRepository;
import com.agrosense.medicion.repository.MedicionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@Transactional
public class MedicionService {

    private final MedicionRepository medicionRepository;
    private final MedicionProcesamientoRepository procesamientoRepository;
    private final AgronomicRulesService rulesService;
    private final RestTemplate restTemplate;

    @Value("${sensor.service.url}")
    private String sensorServiceUrl;

    @Value("${alerta.service.url}")
    private String alertaServiceUrl;

    public MedicionService(
        MedicionRepository medicionRepository,
        MedicionProcesamientoRepository procesamientoRepository,
        AgronomicRulesService rulesService,
        RestTemplate restTemplate
    ) {
        this.medicionRepository = medicionRepository;
        this.procesamientoRepository = procesamientoRepository;
        this.rulesService = rulesService;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public List<MedicionResponseDTO> getAll(UUID sensorId) {
        List<Medicion> list = sensorId != null
            ? medicionRepository.findBySensorIdOrderByFechaDesc(sensorId)
            : medicionRepository.findAllByOrderByFechaDesc();
        return list.stream().map(MedicionResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public MedicionResponseDTO getById(UUID id) {
        return medicionRepository.findById(id)
            .map(MedicionResponseDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Medicion con ID " + id + " no encontrada"));
    }

    public MedicionProcessingResponseDTO create(MedicionCreateDTO dto) {
        // Validar sensor via HTTP al sensor-service
        SensorDTO sensor = getSensorById(dto.sensorId());
        if (!"ACTIVO".equals(sensor.estado())) {
            throw new ConflictException(
                "No se puede registrar la medicion: el sensor con ID " + dto.sensorId() +
                " no esta activo (estado actual: '" + sensor.estado() + "')"
            );
        }

        Medicion nueva = new Medicion(null, dto.temperatura(), dto.humedad(), dto.fecha(), dto.sensorId());
        Medicion guardada = medicionRepository.save(nueva);

        List<AlertaCreateDTO> alertasAEmitir = rulesService.evaluarReglas(
            guardada.getTemperatura(), guardada.getHumedad(), sensor.cultivoId(), dto.fecha()
        );

        if (alertasAEmitir.isEmpty()) {
            procesamientoRepository.save(new MedicionProcesamiento(guardada.getId(), "sin_alertas", "[]", 0, null));
            return new MedicionProcessingResponseDTO(
                MedicionResponseDTO.fromEntity(guardada), List.of(),
                new ProcessingStatusDTO("sin_alertas", 0, 0, "Medicion registrada. Valores dentro de parametros normales.")
            );
        }

        List<AlertaResponseDTO> alertasCreadas = new ArrayList<>();
        for (AlertaCreateDTO alertaDTO : alertasAEmitir) {
            AlertaResponseDTO creada = crearAlerta(alertaDTO);
            if (creada != null) alertasCreadas.add(creada);
        }

        procesamientoRepository.save(new MedicionProcesamiento(guardada.getId(), "completado", alertasCreadas.toString(), 0, null));

        return new MedicionProcessingResponseDTO(
            MedicionResponseDTO.fromEntity(guardada), alertasCreadas,
            new ProcessingStatusDTO("completado", alertasAEmitir.size(), alertasCreadas.size(),
                "Medicion registrada y alertas automaticas procesadas exitosamente.")
        );
    }

    public MedicionProcessingResponseDTO procesarReintento(UUID id) {
        Medicion medicion = medicionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medicion con ID " + id + " no encontrada"));

        SensorDTO sensor = getSensorById(medicion.getSensorId());

        List<AlertaCreateDTO> alertasAEmitir = rulesService.evaluarReglas(
            medicion.getTemperatura(), medicion.getHumedad(), sensor.cultivoId(), medicion.getFecha()
        );

        List<AlertaResponseDTO> alertasCreadas = new ArrayList<>();
        for (AlertaCreateDTO alertaDTO : alertasAEmitir) {
            AlertaResponseDTO creada = crearAlerta(alertaDTO);
            if (creada != null) alertasCreadas.add(creada);
        }

        String estado = alertasAEmitir.isEmpty() ? "sin_alertas" : "completado";
        MedicionProcesamiento proc = procesamientoRepository.findById(medicion.getId())
            .orElse(new MedicionProcesamiento(medicion.getId(), estado, alertasCreadas.toString(), 0, null));
        proc.setEstado(estado);
        proc.setAlertasGeneradas(alertasCreadas.toString());
        proc.setReintentos(proc.getReintentos() + 1);
        procesamientoRepository.save(proc);

        return new MedicionProcessingResponseDTO(
            MedicionResponseDTO.fromEntity(medicion), alertasCreadas,
            new ProcessingStatusDTO(estado, alertasAEmitir.size(), alertasCreadas.size(),
                "Reintento completado. Alertas sincronizadas.")
        );
    }

    // Llama al sensor-service via HTTP
    private SensorDTO getSensorById(UUID sensorId) {
        try {
            ResponseEntity<SensorDTO> response = restTemplate.getForEntity(
                sensorServiceUrl + "/api/sensores/" + sensorId, SensorDTO.class
            );
            if (response.getBody() == null) {
                throw new ResourceNotFoundException("Sensor con ID " + sensorId + " no encontrado");
            }
            return response.getBody();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Sensor con ID " + sensorId + " no encontrado en sensor-service");
        }
    }

    // Llama al alerta-service via HTTP
    private AlertaResponseDTO crearAlerta(AlertaCreateDTO dto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AlertaCreateDTO> request = new HttpEntity<>(dto, headers);
            ResponseEntity<AlertaResponseDTO> response = restTemplate.postForEntity(
                alertaServiceUrl + "/api/alertas", request, AlertaResponseDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // Record interno para deserializar la respuesta del sensor-service
    private record SensorDTO(UUID id, String nombre, String tipo, String estado, UUID cultivoId) {}
}
