package com.agrosense.gestion;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cultivos", schema = "gestion")
public class Cultivo {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "especie", nullable = false, length = 255)
    private String especie;

    @Column(name = "superficie", nullable = false)
    private Double superficie;

    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @Column(name = "productor_id", nullable = false)
    private UUID productorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Cultivo() {
    }

    public Cultivo(UUID id, String especie, Double superficie, LocalDate fechaSiembra, UUID productorId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.especie = especie;
        this.superficie = superficie;
        this.fechaSiembra = fechaSiembra;
        this.productorId = productorId;
        this.createdAt = OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public Double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Double superficie) {
        this.superficie = superficie;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public UUID getProductorId() {
        return productorId;
    }

    public void setProductorId(UUID productorId) {
        this.productorId = productorId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
