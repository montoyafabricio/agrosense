package com.agrosense.service;

import com.agrosense.dto.CultivoCreateDTO;
import com.agrosense.dto.CultivoResponseDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Cultivo;
import com.agrosense.repository.AlertaRepository;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.ProductorRepository;
import com.agrosense.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - CultivoService")
class CultivoServiceTest {

    @Mock
    private CultivoRepository cultivoRepository;
    @Mock
    private ProductorRepository productorRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private CultivoService cultivoService;

    private final UUID cultivoId = UUID.randomUUID();
    private final UUID productorId = UUID.randomUUID();
    private Cultivo mockCultivo;

    @BeforeEach
    void setUp() {
        mockCultivo = new Cultivo(cultivoId, "Trigo", 15.0, LocalDate.of(2026, 5, 20), productorId);
    }

    @Test
    @DisplayName("create() con productor inexistente lanza ResourceNotFoundException")
    void testCreateProductorInexistente() {
        CultivoCreateDTO dto = new CultivoCreateDTO("Maiz", 20.0, LocalDate.now(), productorId);
        when(productorRepository.existsById(productorId)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> cultivoService.create(dto));
        assertTrue(ex.getMessage().contains("El Productor con ID " + productorId + " no existe"));
        verify(cultivoRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() con productor existente guarda y retorna Cultivo")
    void testCreateExitoso() {
        CultivoCreateDTO dto = new CultivoCreateDTO("Trigo", 15.0, LocalDate.of(2026, 5, 20), productorId);
        when(productorRepository.existsById(productorId)).thenReturn(true);
        when(cultivoRepository.save(any(Cultivo.class))).thenReturn(mockCultivo);

        CultivoResponseDTO result = cultivoService.create(dto);

        assertNotNull(result);
        assertEquals("Trigo", result.especie());
        assertEquals(15.0, result.superficie());
        verify(cultivoRepository, times(1)).save(any(Cultivo.class));
    }

    @Test
    @DisplayName("delete() con sensores asociados lanza ConflictException")
    void testDeleteConSensoresLanzaConflicto() {
        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(sensorRepository.countByCultivoId(cultivoId)).thenReturn(3L);

        ConflictException ex = assertThrows(ConflictException.class, () -> cultivoService.delete(cultivoId));
        assertTrue(ex.getMessage().contains("tiene 3 sensor(es) asociado(s)"));
        verify(cultivoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete() con alertas asociadas lanza ConflictException")
    void testDeleteConAlertasLanzaConflicto() {
        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(sensorRepository.countByCultivoId(cultivoId)).thenReturn(0L);
        when(alertaRepository.countByCultivoId(cultivoId)).thenReturn(1L);

        ConflictException ex = assertThrows(ConflictException.class, () -> cultivoService.delete(cultivoId));
        assertTrue(ex.getMessage().contains("tiene 1 alerta(s) asociada(s)"));
        verify(cultivoRepository, never()).deleteById(any());
    }
}
