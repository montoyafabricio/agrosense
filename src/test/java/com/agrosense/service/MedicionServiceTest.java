package com.agrosense.service;

import com.agrosense.dto.*;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.*;
import com.agrosense.repository.CultivoRepository;
import com.agrosense.repository.MedicionProcesamientoRepository;
import com.agrosense.repository.MedicionRepository;
import com.agrosense.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - MedicionService")
class MedicionServiceTest {

    @Mock
    private MedicionRepository medicionRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private CultivoRepository cultivoRepository;
    @Mock
    private AlertaService alertaService;
    @Mock
    private MedicionProcesamientoRepository procesamientoRepository;
    @Mock
    private AgronomicRulesService rulesService;

    @InjectMocks
    private MedicionService medicionService;

    private final UUID sensorId = UUID.randomUUID();
    private final UUID cultivoId = UUID.randomUUID();
    private final UUID medicionId = UUID.randomUUID();
    private Sensor activoSensor;
    private Sensor inactivoSensor;

    @BeforeEach
    void setUp() {
        activoSensor = new Sensor(sensorId, SensorTipo.MIXTO, "Sector A", SensorEstado.ACTIVO, cultivoId);
        inactivoSensor = new Sensor(sensorId, SensorTipo.MIXTO, "Sector A", SensorEstado.INACTIVO, cultivoId);
    }

    @Test
    @DisplayName("create() con sensor inexistente lanza ResourceNotFoundException")
    void testCreateSensorInexistente() {
        MedicionCreateDTO dto = new MedicionCreateDTO(25.0, 50.0, OffsetDateTime.now(), sensorId);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicionService.create(dto));
        verify(medicionRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() con sensor no activo lanza ConflictException")
    void testCreateSensorInactivoLanzaConflicto() {
        MedicionCreateDTO dto = new MedicionCreateDTO(25.0, 50.0, OffsetDateTime.now(), sensorId);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(inactivoSensor));

        ConflictException ex = assertThrows(ConflictException.class, () -> medicionService.create(dto));
        assertTrue(ex.getMessage().contains("no está activo"));
        verify(medicionRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() con valores normales persiste medicion con estado 'sin_alertas'")
    void testCreateValoresNormalesSinAlertas() {
        OffsetDateTime now = OffsetDateTime.now();
        MedicionCreateDTO dto = new MedicionCreateDTO(24.0, 50.0, now, sensorId);
        Medicion savedMedicion = new Medicion(medicionId, 24.0, 50.0, now, sensorId);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(activoSensor));
        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(medicionRepository.save(any(Medicion.class))).thenReturn(savedMedicion);
        when(rulesService.evaluarReglas(24.0, 50.0, cultivoId, now)).thenReturn(List.of());

        MedicionProcessingResponseDTO response = medicionService.create(dto);

        assertNotNull(response);
        assertEquals(medicionId, response.medicion().id());
        assertTrue(response.alertas().isEmpty());
        assertEquals("sin_alertas", response.procesamiento().estado());
        assertEquals(0, response.procesamiento().totalAlertas());
        verify(procesamientoRepository, times(1)).save(any(MedicionProcesamiento.class));
    }

    @Test
    @DisplayName("create() con condiciones extremas genera y persiste alertas con estado 'completado'")
    void testCreateCondicionesExtremasGeneraAlertas() {
        OffsetDateTime now = OffsetDateTime.now();
        MedicionCreateDTO dto = new MedicionCreateDTO(38.0, 20.0, now, sensorId);
        Medicion savedMedicion = new Medicion(medicionId, 38.0, 20.0, now, sensorId);

        AlertaCreateDTO alertaRiego = new AlertaCreateDTO("RIEGO", "Revisar riego", AlertaPrioridad.ALTA, now, cultivoId);
        AlertaCreateDTO alertaCalor = new AlertaCreateDTO("CALOR", "Temperatura elevada", AlertaPrioridad.ALTA, now, cultivoId);

        AlertaResponseDTO resRiego = new AlertaResponseDTO(UUID.randomUUID(), "RIEGO", "Revisar riego", AlertaPrioridad.ALTA, now, cultivoId);
        AlertaResponseDTO resCalor = new AlertaResponseDTO(UUID.randomUUID(), "CALOR", "Temperatura elevada", AlertaPrioridad.ALTA, now, cultivoId);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(activoSensor));
        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(medicionRepository.save(any(Medicion.class))).thenReturn(savedMedicion);
        when(rulesService.evaluarReglas(38.0, 20.0, cultivoId, now)).thenReturn(List.of(alertaRiego, alertaCalor));
        when(alertaService.create(alertaRiego)).thenReturn(resRiego);
        when(alertaService.create(alertaCalor)).thenReturn(resCalor);

        MedicionProcessingResponseDTO response = medicionService.create(dto);

        assertNotNull(response);
        assertEquals(2, response.alertas().size());
        assertEquals("completado", response.procesamiento().estado());
        assertEquals(2, response.procesamiento().totalAlertas());
        assertEquals(2, response.procesamiento().alertasEnviadas());
        verify(alertaService, times(2)).create(any());
        verify(procesamientoRepository, times(1)).save(any(MedicionProcesamiento.class));
    }
}
