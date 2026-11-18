package com.agrosense.gestion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    private final CultivoService cultivoService;

    public CultivoController(CultivoService cultivoService) {
        this.cultivoService = cultivoService;
    }

    @GetMapping
    public ResponseEntity<List<CultivoResponseDTO>> getAll() {
        return ResponseEntity.ok(cultivoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CultivoResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(cultivoService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CultivoResponseDTO> create(@Valid @RequestBody CultivoCreateDTO dto) {
        CultivoResponseDTO created = cultivoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CultivoResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody CultivoUpdateDTO dto) {
        return ResponseEntity.ok(cultivoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cultivoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
