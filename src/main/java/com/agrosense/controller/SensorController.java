package com.agrosense.controller;

import com.agrosense.dto.SensorCreateDTO;
import com.agrosense.dto.SensorResponseDTO;
import com.agrosense.dto.SensorUpdateDTO;
import com.agrosense.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sensores")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public ResponseEntity<List<SensorResponseDTO>> getAll(@RequestParam(required = false) UUID cultivoId) {
        return ResponseEntity.ok(sensorService.getAll(cultivoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sensorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SensorResponseDTO> create(@Valid @RequestBody SensorCreateDTO dto) {
        SensorResponseDTO created = sensorService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody SensorUpdateDTO dto) {
        return ResponseEntity.ok(sensorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sensorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
