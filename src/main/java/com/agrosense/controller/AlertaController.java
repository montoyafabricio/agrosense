package com.agrosense.controller;

import com.agrosense.dto.AlertaCreateDTO;
import com.agrosense.dto.AlertaResponseDTO;
import com.agrosense.model.AlertaPrioridad;
import com.agrosense.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponseDTO>> getAll(
        @RequestParam(required = false) UUID cultivoId,
        @RequestParam(required = false) AlertaPrioridad prioridad
    ) {
        return ResponseEntity.ok(alertaService.getAll(cultivoId, prioridad));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(alertaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AlertaResponseDTO> create(@Valid @RequestBody AlertaCreateDTO dto) {
        AlertaResponseDTO created = alertaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
