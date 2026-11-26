package com.agrosense.controller;

import com.agrosense.dto.MedicionCreateDTO;
import com.agrosense.dto.MedicionProcessingResponseDTO;
import com.agrosense.dto.MedicionResponseDTO;
import com.agrosense.dto.ProcessingStatusDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.service.MedicionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicionController.class)
@DisplayName("Controller Slice Tests - MedicionController")
class MedicionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicionService medicionService;

    private final UUID sensorId = UUID.randomUUID();
    private final UUID medicionId = UUID.randomUUID();

    @Test
    @DisplayName("POST /api/mediciones con sensor inactivo retorna 409 ConflictError")
    void testCreateSensorInactivoRetornaConflicto() throws Exception {
        MedicionCreateDTO request = new MedicionCreateDTO(25.0, 50.0, OffsetDateTime.now(), sensorId);

        when(medicionService.create(any(MedicionCreateDTO.class)))
            .thenThrow(new ConflictException("No se puede registrar la medición: el sensor con ID " + sensorId + " no está activo"));

        mockMvc.perform(post("/api/mediciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("ConflictError"));
    }

    @Test
    @DisplayName("POST /api/mediciones con medicion valida retorna 201 y procesamiento")
    void testCreateMedicionExitoso() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        MedicionCreateDTO request = new MedicionCreateDTO(24.0, 50.0, now, sensorId);

        MedicionResponseDTO medResponse = new MedicionResponseDTO(medicionId, 24.0, 50.0, now, sensorId);
        MedicionProcessingResponseDTO response = new MedicionProcessingResponseDTO(
            medResponse,
            List.of(),
            new ProcessingStatusDTO("sin_alertas", 0, 0, "Normal")
        );

        when(medicionService.create(any(MedicionCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/mediciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.medicion.id").value(medicionId.toString()))
            .andExpect(jsonPath("$.procesamiento.estado").value("sin_alertas"));
    }
}
