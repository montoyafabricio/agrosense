package com.agrosense.controller;

import com.agrosense.dto.ProductorCreateDTO;
import com.agrosense.dto.ProductorResponseDTO;
import com.agrosense.exception.ConflictException;
import com.agrosense.exception.ResourceNotFoundException;
import com.agrosense.service.ProductorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductorController.class)
@DisplayName("Controller Slice Tests - ProductorController")
class ProductorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductorService productorService;

    private final UUID id = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/productores retorna 200 y lista de productores")
    void testGetAll() throws Exception {
        ProductorResponseDTO dto = new ProductorResponseDTO(id, "Carlos Gomez", "carlos@agrosense.com");
        when(productorService.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/productores"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(id.toString()))
            .andExpect(jsonPath("$[0].nombre").value("Carlos Gomez"));
    }

    @Test
    @DisplayName("GET /api/productores/{id} con ID existente retorna 200")
    void testGetByIdExitoso() throws Exception {
        ProductorResponseDTO dto = new ProductorResponseDTO(id, "Carlos Gomez", "carlos@agrosense.com");
        when(productorService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/productores/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.nombre").value("Carlos Gomez"));
    }

    @Test
    @DisplayName("GET /api/productores/{id} con ID no existente retorna 404")
    void testGetByIdNoEncontrado() throws Exception {
        when(productorService.getById(id)).thenThrow(new ResourceNotFoundException("Productor con ID " + id + " no encontrado"));

        mockMvc.perform(get("/api/productores/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NotFoundError"))
            .andExpect(jsonPath("$.error.message").value("Productor con ID " + id + " no encontrado"));
    }

    @Test
    @DisplayName("POST /api/productores con datos validos retorna 201")
    void testCreateExitoso() throws Exception {
        ProductorCreateDTO request = new ProductorCreateDTO("Ana Perez", "ana@agrosense.com");
        ProductorResponseDTO response = new ProductorResponseDTO(id, "Ana Perez", "ana@agrosense.com");

        when(productorService.create(any(ProductorCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/productores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.nombre").value("Ana Perez"));
    }

    @Test
    @DisplayName("POST /api/productores con nombre vacio retorna 400 ValidationError")
    void testCreateValidacionFalla() throws Exception {
        ProductorCreateDTO request = new ProductorCreateDTO("", "ana@agrosense.com");

        mockMvc.perform(post("/api/productores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("ValidationError"));
    }

    @Test
    @DisplayName("DELETE /api/productores/{id} con conflicto de integridad retorna 409 ConflictError")
    void testDeleteConflicto() throws Exception {
        doThrow(new ConflictException("No se puede eliminar el productor: tiene 2 cultivo(s) asociado(s)"))
            .when(productorService).delete(id);

        mockMvc.perform(delete("/api/productores/{id}", id))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("ConflictError"))
            .andExpect(jsonPath("$.error.message").value("No se puede eliminar el productor: tiene 2 cultivo(s) asociado(s)"));
    }

    @Test
    @DisplayName("DELETE /api/productores/{id} sin asociaciones retorna 204 NoContent")
    void testDeleteExitoso() throws Exception {
        mockMvc.perform(delete("/api/productores/{id}", id))
            .andExpect(status().isNoContent());
    }
}
