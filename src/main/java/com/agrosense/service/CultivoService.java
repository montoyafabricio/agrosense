package com.agrosense.service;

import com.agrosense.dto.CultivoCreateDTO;
import com.agrosense.dto.CultivoResponseDTO;
import com.agrosense.dto.CultivoUpdateDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Cultivo;
import com.agrosense.repository.AlertaRepository;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.ProductorRepository;
import com.agrosense.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CultivoService {

    private final CultivoRepository cultivoRepository;
    private final ProductorRepository productorRepository;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;

    public CultivoService(
        CultivoRepository cultivoRepository,
        ProductorRepository productorRepository,
        SensorRepository sensorRepository,
        AlertaRepository alertaRepository
    ) {
        this.cultivoRepository = cultivoRepository;
        this.productorRepository = productorRepository;
        this.sensorRepository = sensorRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<CultivoResponseDTO> getAll() {
        return cultivoRepository.findAllByOrderByCreatedAtAsc()
            .stream()
            .map(CultivoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CultivoResponseDTO getById(UUID id) {
        Cultivo cultivo = cultivoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cultivo con ID " + id + " no encontrado"));
        return CultivoResponseDTO.fromEntity(cultivo);
    }

    public CultivoResponseDTO create(CultivoCreateDTO dto) {
        if (!productorRepository.existsById(dto.productorId())) {
            throw new ResourceNotFoundException("No se puede crear el cultivo: El Productor con ID " + dto.productorId() + " no existe");
        }

        Cultivo nuevo = new Cultivo(null, dto.especie(), dto.superficie(), dto.fechaSiembra(), dto.productorId());
        Cultivo guardado = cultivoRepository.save(nuevo);
        return CultivoResponseDTO.fromEntity(guardado);
    }

    public CultivoResponseDTO update(UUID id, CultivoUpdateDTO dto) {
        Cultivo cultivo = cultivoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cultivo con ID " + id + " no encontrado"));

        if (!productorRepository.existsById(dto.productorId())) {
            throw new ResourceNotFoundException("El Productor con ID " + dto.productorId() + " no existe");
        }

        cultivo.setEspecie(dto.especie());
        cultivo.setSuperficie(dto.superficie());
        cultivo.setFechaSiembra(dto.fechaSiembra());
        cultivo.setProductorId(dto.productorId());

        Cultivo actualizado = cultivoRepository.save(cultivo);
        return CultivoResponseDTO.fromEntity(actualizado);
    }

    public void delete(UUID id) {
        if (!cultivoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cultivo con ID " + id + " no encontrado");
        }

        long sensorCount = sensorRepository.countByCultivoId(id);
        if (sensorCount > 0) {
            throw new ConflictException("No se puede eliminar el cultivo: tiene " + sensorCount + " sensor(es) asociado(s)");
        }

        long alertaCount = alertaRepository.countByCultivoId(id);
        if (alertaCount > 0) {
            throw new ConflictException("No se puede eliminar el cultivo: tiene " + alertaCount + " alerta(s) asociada(s)");
        }

        cultivoRepository.deleteById(id);
    }
}
