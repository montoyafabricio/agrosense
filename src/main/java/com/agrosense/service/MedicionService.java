package com.agrosense.service;

import com.agrosense.dto.*;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Medicion;
import com.agrosense.model.MedicionProcesamiento;
import com.agrosense.model.Sensor;
import com.agrosense.model.SensorEstado;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.MedicionProcesamientoRepository;
import com.agrosense.repository.MedicionRepository;
import com.agrosense.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicionService {

    private final MedicionRepository medicionRepository;
    private final SensorRepository sensorRepository;
    private final CultivoRepository cultivoRepository;
    private final AlertaService alertaService;
    private final MedicionProcesamientoRepository procesamientoRepository;
    private final AgronomicRulesService rulesService;

    public MedicionService(
        MedicionRepository medicionRepository,
        SensorRepository sensorRepository,
        CultivoRepository cultivoRepository,
        AlertaService alertaService,
        MedicionProcesamientoRepository procesamientoRepository,
        AgronomicRulesService rulesService
    ) {
        this.medicionRepository = medicionRepository;
        this.sensorRepository = sensorRepository;
        this.cultivoRepository = cultivoRepository;
        this.alertaService = alertaService;
        this.procesamientoRepository = procesamientoRepository;
        this.rulesService = rulesService;
    }

    @Transactional(readOnly = true)
    public List<MedicionResponseDTO> getAll(UUID sensorId) {
        List<Medicion> list = sensorId != null
            ? medicionRepository.findBySensorIdOrderByFechaDesc(sensorId)
            : medicionRepository.findAllByOrderByFechaDesc();

        return list.stream()
            .map(MedicionResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public MedicionResponseDTO getById(UUID id) {
        Medicion medicion = medicionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medición con ID " + id + " no encontrada"));
        return MedicionResponseDTO.fromEntity(medicion);
    }

    public MedicionProcessingResponseDTO create(MedicionCreateDTO dto) {
        Sensor sensor = sensorRepository.findById(dto.sensorId())
            .orElseThrow(() -> new ResourceNotFoundException("Sensor con ID " + dto.sensorId() + " no encontrado"));

        if (sensor.getEstado() != SensorEstado.ACTIVO) {
            throw new ConflictException(
                "No se puede registrar la medición: el sensor con ID " + dto.sensorId() +
                " no está activo (estado actual: '" + sensor.getEstado().getValue() + "')"
            );
        }

        if (!cultivoRepository.existsById(sensor.getCultivoId())) {
            throw new ResourceNotFoundException("El Cultivo con ID " + sensor.getCultivoId() + " no existe");
        }

        Medicion nueva = new Medicion(null, dto.temperatura(), dto.humedad(), dto.fecha(), dto.sensorId());
        Medicion guardada = medicionRepository.save(nueva);

        List<AlertaCreateDTO> alertasAEmitir = rulesService.evaluarReglas(
            guardada.getTemperatura(),
            guardada.getHumedad(),
            sensor.getCultivoId(),
            dto.fecha()
        );

        if (alertasAEmitir.isEmpty()) {
            MedicionProcesamiento proc = new MedicionProcesamiento(
                guardada.getId(),
                "sin_alertas",
                "[]",
                0,
                null
            );
            procesamientoRepository.save(proc);

            return new MedicionProcessingResponseDTO(
                MedicionResponseDTO.fromEntity(guardada),
                List.of(),
                new ProcessingStatusDTO(
                    "sin_alertas",
                    0,
                    0,
                    "Medición registrada. Los valores evaluados se encuentran dentro de los parámetros normales."
                )
            );
        }

        List<AlertaResponseDTO> alertasCreadas = new ArrayList<>();
        for (AlertaCreateDTO alertaDTO : alertasAEmitir) {
            AlertaResponseDTO creada = alertaService.create(alertaDTO);
            alertasCreadas.add(creada);
        }

        MedicionProcesamiento proc = new MedicionProcesamiento(
            guardada.getId(),
            "completado",
            alertasCreadas.toString(),
            0,
            null
        );
        procesamientoRepository.save(proc);

        return new MedicionProcessingResponseDTO(
            MedicionResponseDTO.fromEntity(guardada),
            alertasCreadas,
            new ProcessingStatusDTO(
                "completado",
                alertasAEmitir.size(),
                alertasCreadas.size(),
                "Medición registrada y alertas automáticas procesadas exitosamente."
            )
        );
    }

    public MedicionProcessingResponseDTO procesarReintento(UUID id) {
        Medicion medicion = medicionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medición con ID " + id + " no encontrada"));

        Sensor sensor = sensorRepository.findById(medicion.getSensorId())
            .orElseThrow(() -> new ResourceNotFoundException("Sensor con ID " + medicion.getSensorId() + " no encontrado"));

        List<AlertaCreateDTO> alertasAEmitir = rulesService.evaluarReglas(
            medicion.getTemperatura(),
            medicion.getHumedad(),
            sensor.getCultivoId(),
            medicion.getFecha()
        );

        List<AlertaResponseDTO> alertasCreadas = new ArrayList<>();
        for (AlertaCreateDTO alertaDTO : alertasAEmitir) {
            AlertaResponseDTO creada = alertaService.create(alertaDTO);
            alertasCreadas.add(creada);
        }

        String estado = alertasAEmitir.isEmpty() ? "sin_alertas" : "completado";
        MedicionProcesamiento proc = procesamientoRepository.findById(medicion.getId())
            .orElse(new MedicionProcesamiento(medicion.getId(), estado, alertasCreadas.toString(), 0, null));

        proc.setEstado(estado);
        proc.setAlertasGeneradas(alertasCreadas.toString());
        proc.setReintentos(proc.getReintentos() + 1);
        procesamientoRepository.save(proc);

        return new MedicionProcessingResponseDTO(
            MedicionResponseDTO.fromEntity(medicion),
            alertasCreadas,
            new ProcessingStatusDTO(
                estado,
                alertasAEmitir.size(),
                alertasCreadas.size(),
                "Reintento completado con éxito. Todas las alertas fueron sincronizadas."
            )
        );
    }
}
