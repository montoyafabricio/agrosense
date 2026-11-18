package com.agrosense.service;

import com.agrosense.dto.ProductorCreateDTO;
import com.agrosense.dto.ProductorResponseDTO;
import com.agrosense.dto.ProductorUpdateDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Productor;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.ProductorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductorService {

    private final ProductorRepository productorRepository;
    private final CultivoRepository cultivoRepository;

    public ProductorService(ProductorRepository productorRepository, CultivoRepository cultivoRepository) {
        this.productorRepository = productorRepository;
        this.cultivoRepository = cultivoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductorResponseDTO> getAll() {
        return productorRepository.findAllByOrderByCreatedAtAsc()
            .stream()
            .map(ProductorResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductorResponseDTO getById(UUID id) {
        Productor productor = productorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Productor con ID " + id + " no encontrado"));
        return ProductorResponseDTO.fromEntity(productor);
    }

    public ProductorResponseDTO create(ProductorCreateDTO dto) {
        Productor nuevo = new Productor(null, dto.nombre(), dto.contacto());
        Productor guardado = productorRepository.save(nuevo);
        return ProductorResponseDTO.fromEntity(guardado);
    }

    public ProductorResponseDTO update(UUID id, ProductorUpdateDTO dto) {
        Productor productor = productorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Productor con ID " + id + " no encontrado"));

        productor.setNombre(dto.nombre());
        productor.setContacto(dto.contacto());
        Productor actualizado = productorRepository.save(productor);
        return ProductorResponseDTO.fromEntity(actualizado);
    }

    public void delete(UUID id) {
        if (!productorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Productor con ID " + id + " no encontrado");
        }

        long count = cultivoRepository.countByProductorId(id);
        if (count > 0) {
            throw new ConflictException("No se puede eliminar el productor: tiene " + count + " cultivo(s) asociado(s)");
        }

        productorRepository.deleteById(id);
    }
}
