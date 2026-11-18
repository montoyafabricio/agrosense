package com.agrosense.sensor;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensores", schema = "sensores")
public class Sensor {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private SensorTipo tipo;

    @Column(name = "ubicacion", nullable = false, length = 255)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private SensorEstado estado;

    @Column(name = "cultivo_id", nullable = false)
    private UUID cultivoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Sensor() {
    }

    public Sensor(UUID id, SensorTipo tipo, String ubicacion, SensorEstado estado, UUID cultivoId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.cultivoId = cultivoId;
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

    public SensorTipo getTipo() {
        return tipo;
    }

    public void setTipo(SensorTipo tipo) {
        this.tipo = tipo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public SensorEstado getEstado() {
        return estado;
    }

    public void setEstado(SensorEstado estado) {
        this.estado = estado;
    }

    public UUID getCultivoId() {
        return cultivoId;
    }

    public void setCultivoId(UUID cultivoId) {
        this.cultivoId = cultivoId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
