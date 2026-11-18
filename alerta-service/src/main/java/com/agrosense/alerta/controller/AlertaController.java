package com.agrosense.alerta.controller;

import com.agrosense.alerta.dto.AlertaCreateDTO;
import com.agrosense.alerta.dto.AlertaResponseDTO;
import com.agrosense.alerta.model.AlertaPrioridad;
import com.agrosense.alerta.service.AlertaService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.create(dto));
    }
}
