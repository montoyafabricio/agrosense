package com.agrosense.service;

import com.agrosense.dto.ProductorCreateDTO;
import com.agrosense.dto.ProductorResponseDTO;
import com.agrosense.dto.ProductorUpdateDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Productor;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.ProductorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - ProductorService")
class ProductorServiceTest {

    @Mock
    private ProductorRepository productorRepository;

    @Mock
    private CultivoRepository cultivoRepository;

    @InjectMocks
    private ProductorService productorService;

    private Productor mockProductor;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockProductor = new Productor(id, "Carlos Gomez", "carlos@agrosense.com");
    }

    @Test
    @DisplayName("getAll() retorna todos los productores ordenados")
    void testGetAll() {
        when(productorRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(mockProductor));

        List<ProductorResponseDTO> result = productorService.getAll();

        assertEquals(1, result.size());
        assertEquals("Carlos Gomez", result.getFirst().nombre());
        verify(productorRepository, times(1)).findAllByOrderByCreatedAtAsc();
    }

    @Test
    @DisplayName("getById() con ID existente retorna el productor")
    void testGetByIdExitoso() {
        when(productorRepository.findById(id)).thenReturn(Optional.of(mockProductor));

        ProductorResponseDTO result = productorService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Carlos Gomez", result.nombre());
    }

    @Test
    @DisplayName("getById() con ID inexistente lanza ResourceNotFoundException")
    void testGetByIdNoEncontrado() {
        when(productorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productorService.getById(id));
    }

    @Test
    @DisplayName("create() guarda y retorna el nuevo productor")
    void testCreate() {
        ProductorCreateDTO createDto = new ProductorCreateDTO("Laura Paez", "laura@agrosense.com");
        Productor saved = new Productor(UUID.randomUUID(), "Laura Paez", "laura@agrosense.com");

        when(productorRepository.save(any(Productor.class))).thenReturn(saved);

        ProductorResponseDTO result = productorService.create(createDto);

        assertNotNull(result);
        assertEquals("Laura Paez", result.nombre());
        assertEquals("laura@agrosense.com", result.contacto());
        verify(productorRepository, times(1)).save(any(Productor.class));
    }

    @Test
    @DisplayName("delete() lanza ConflictException si tiene cultivos asociados")
    void testDeleteConCultivosLanzaConflicto() {
        when(productorRepository.existsById(id)).thenReturn(true);
        when(cultivoRepository.countByProductorId(id)).thenReturn(2L);

        ConflictException ex = assertThrows(ConflictException.class, () -> productorService.delete(id));
        assertTrue(ex.getMessage().contains("tiene 2 cultivo(s) asociado(s)"));
        verify(productorRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete() elimina satisfactoriamente si no tiene cultivos asociados")
    void testDeleteSinCultivosExitoso() {
        when(productorRepository.existsById(id)).thenReturn(true);
        when(cultivoRepository.countByProductorId(id)).thenReturn(0L);

        assertDoesNotThrow(() -> productorService.delete(id));
        verify(productorRepository, times(1)).deleteById(id);
    }
}
