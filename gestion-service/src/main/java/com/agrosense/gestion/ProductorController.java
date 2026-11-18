package com.agrosense.gestion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productores")
public class ProductorController {

    private final ProductorService productorService;

    public ProductorController(ProductorService productorService) {
        this.productorService = productorService;
    }

    @GetMapping
    public ResponseEntity<List<ProductorResponseDTO>> getAll() {
        return ResponseEntity.ok(productorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductorResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductorResponseDTO> create(@Valid @RequestBody ProductorCreateDTO dto) {
        ProductorResponseDTO created = productorService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductorResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ProductorUpdateDTO dto) {
        return ResponseEntity.ok(productorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
