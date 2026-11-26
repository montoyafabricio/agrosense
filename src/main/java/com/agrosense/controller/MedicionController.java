package com.agrosense.controller;

import com.agrosense.dto.MedicionCreateDTO;
import com.agrosense.dto.MedicionProcessingResponseDTO;
import com.agrosense.dto.MedicionResponseDTO;
import com.agrosense.service.MedicionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mediciones")
public class MedicionController {

    private final MedicionService medicionService;

    public MedicionController(MedicionService medicionService) {
        this.medicionService = medicionService;
    }

    @GetMapping
    public ResponseEntity<List<MedicionResponseDTO>> getAll(@RequestParam(required = false) UUID sensorId) {
        return ResponseEntity.ok(medicionService.getAll(sensorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicionResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MedicionProcessingResponseDTO> create(@Valid @RequestBody MedicionCreateDTO dto) {
        MedicionProcessingResponseDTO response = medicionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/procesar")
    public ResponseEntity<MedicionProcessingResponseDTO> procesarReintento(@PathVariable UUID id) {
        return ResponseEntity.ok(medicionService.procesarReintento(id));
    }
}
