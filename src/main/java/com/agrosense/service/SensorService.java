package com.agrosense.service;

import com.agrosense.dto.SensorCreateDTO;
import com.agrosense.dto.SensorResponseDTO;
import com.agrosense.dto.SensorUpdateDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Sensor;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.MedicionRepository;
import com.agrosense.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SensorService {

    private final SensorRepository sensorRepository;
    private final CultivoRepository cultivoRepository;
    private final MedicionRepository medicionRepository;

    public SensorService(
        SensorRepository sensorRepository,
        CultivoRepository cultivoRepository,
        MedicionRepository medicionRepository
    ) {
        this.sensorRepository = sensorRepository;
        this.cultivoRepository = cultivoRepository;
        this.medicionRepository = medicionRepository;
    }

    @Transactional(readOnly = true)
    public List<SensorResponseDTO> getAll(UUID cultivoId) {
        List<Sensor> sensors = cultivoId != null
            ? sensorRepository.findByCultivoIdOrderByCreatedAtAsc(cultivoId)
            : sensorRepository.findAllByOrderByCreatedAtAsc();

        return sensors.stream()
            .map(SensorResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public SensorResponseDTO getById(UUID id) {
        Sensor sensor = sensorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor con ID " + id + " no encontrado"));
        return SensorResponseDTO.fromEntity(sensor);
    }

    public SensorResponseDTO create(SensorCreateDTO dto) {
        if (!cultivoRepository.existsById(dto.cultivoId())) {
            throw new ResourceNotFoundException("No se puede crear el sensor: El Cultivo con ID " + dto.cultivoId() + " no existe");
        }

        Sensor nuevo = new Sensor(null, dto.tipo(), dto.ubicacion(), dto.estado(), dto.cultivoId());
        Sensor guardado = sensorRepository.save(nuevo);
        return SensorResponseDTO.fromEntity(guardado);
    }

    public SensorResponseDTO update(UUID id, SensorUpdateDTO dto) {
        Sensor sensor = sensorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sensor con ID " + id + " no encontrado"));

        if (!cultivoRepository.existsById(dto.cultivoId())) {
            throw new ResourceNotFoundException("El Cultivo con ID " + dto.cultivoId() + " no existe");
        }

        sensor.setTipo(dto.tipo());
        sensor.setUbicacion(dto.ubicacion());
        sensor.setEstado(dto.estado());
        sensor.setCultivoId(dto.cultivoId());

        Sensor actualizado = sensorRepository.save(sensor);
        return SensorResponseDTO.fromEntity(actualizado);
    }

    public void delete(UUID id) {
        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor con ID " + id + " no encontrado");
        }

        long medicionesCount = medicionRepository.countBySensorId(id);
        if (medicionesCount > 0) {
            throw new ConflictException("No se puede eliminar el sensor: tiene " + medicionesCount + " medición(es) histórica(s) registrada(s)");
        }

        sensorRepository.deleteById(id);
    }
}
