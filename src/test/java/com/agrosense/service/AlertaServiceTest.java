package com.agrosense.service;

import com.agrosense.dto.AlertaCreateDTO;
import com.agrosense.dto.AlertaResponseDTO;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.model.Alerta;
import com.agrosense.model.AlertaPrioridad;
import com.agrosense.repository.AlertaRepository;
import com.agrosense.repository.CultivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - AlertaService")
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private CultivoRepository cultivoRepository;

    @InjectMocks
    private AlertaService alertaService;

    private final UUID cultivoId = UUID.randomUUID();
    private final UUID alertaId = UUID.randomUUID();
    private final OffsetDateTime fecha = OffsetDateTime.now();

    @Test
    @DisplayName("create() con cultivo inexistente lanza ResourceNotFoundException")
    void testCreateCultivoInexistente() {
        AlertaCreateDTO dto = new AlertaCreateDTO("RIEGO", "Revisar riego", AlertaPrioridad.ALTA, fecha, cultivoId);
        when(cultivoRepository.existsById(cultivoId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> alertaService.create(dto));
        verify(alertaRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() con duplicado dentro de ventana de 120s retorna la alerta existente (Idempotencia)")
    void testCreateDuplicadoRetornaExistente() {
        AlertaCreateDTO dto = new AlertaCreateDTO("RIEGO", "Revisar riego", AlertaPrioridad.ALTA, fecha, cultivoId);
        Alerta existente = new Alerta(alertaId, "RIEGO", "Revisar riego", AlertaPrioridad.ALTA, fecha, cultivoId);

        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(alertaRepository.findDuplicates(eq(cultivoId), eq("RIEGO"), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(List.of(existente));

        AlertaResponseDTO result = alertaService.create(dto);

        assertNotNull(result);
        assertEquals(alertaId, result.id());
        assertEquals("RIEGO", result.tipo());
        verify(alertaRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() sin duplicado persiste nueva alerta")
    void testCreateNuevaAlerta() {
        AlertaCreateDTO dto = new AlertaCreateDTO("CALOR", "Calor extremo", AlertaPrioridad.ALTA, fecha, cultivoId);
        Alerta nueva = new Alerta(alertaId, "CALOR", "Calor extremo", AlertaPrioridad.ALTA, fecha, cultivoId);

        when(cultivoRepository.existsById(cultivoId)).thenReturn(true);
        when(alertaRepository.findDuplicates(eq(cultivoId), eq("CALOR"), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(List.of());
        when(alertaRepository.save(any(Alerta.class))).thenReturn(nueva);

        AlertaResponseDTO result = alertaService.create(dto);

        assertNotNull(result);
        assertEquals(alertaId, result.id());
        assertEquals("CALOR", result.tipo());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }
}
