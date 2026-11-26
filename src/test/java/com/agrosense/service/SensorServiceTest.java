package com.agrosense.service;

import com.agrosense.dto.SensorCreateDTO;
import com.agrosense.dto.SensorResponseDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Sensor;
import com.agrosense.model.SensorEstado;
import com.agrosense.model.SensorTipo;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.MedicionRepository;
import com.agrosense.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - SensorService")
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private CultivoRepository cultivoRepository;
    @Mock
    private MedicionRepository medicionRepository;

    @InjectMocks
    private SensorService sensorService;

    private final UUID sensorId = UUID.randomUUID();
    private final UUID cultivoId = UUID.randomUUID();
    private Sensor mockSensor;

    @BeforeEach
    void setUp() {
        mockSensor = new Sensor(sensorId, SensorTipo.HUMEDAD, "Lote Sur", SensorEstado.ACTIVO, cultivoId);
    }

    @Test
    @DisplayName("create() con cultivo inexistente lanza ResourceNotFoundException")
    void testCreateCultivoInexistente() {
        SensorCreateDTO dto = new SensorCreateDTO(SensorTipo.HUMEDAD, "Lote 1", SensorEstado.ACTIVO, cultivoId);
        when(cultivoRepository.existsById(cultivoId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> sensorService.create(dto));
        verify(sensorRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete() con mediciones historicas registradas lanza ConflictException")
    void testDeleteConMedicionesHistoricasLanzaConflicto() {
        when(sensorRepository.existsById(sensorId)).thenReturn(true);
        when(medicionRepository.countBySensorId(sensorId)).thenReturn(10L);

        ConflictException ex = assertThrows(ConflictException.class, () -> sensorService.delete(sensorId));
        assertTrue(ex.getMessage().contains("tiene 10 medición(es) histórica(s) registrada(s)"));
        verify(sensorRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete() sin mediciones elimina el sensor correctamente")
    void testDeleteExitoso() {
        when(sensorRepository.existsById(sensorId)).thenReturn(true);
        when(medicionRepository.countBySensorId(sensorId)).thenReturn(0L);

        assertDoesNotThrow(() -> sensorService.delete(sensorId));
        verify(sensorRepository, times(1)).deleteById(sensorId);
    }
}
