package com.agrosense.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mediciones", schema = "mediciones")
public class Medicion {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "temperatura", nullable = false)
    private Double temperatura;

    @Column(name = "humedad", nullable = false)
    private Double humedad;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Medicion() {
    }

    public Medicion(UUID id, Double temperatura, Double humedad, OffsetDateTime fecha, UUID sensorId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.fecha = fecha != null ? fecha : OffsetDateTime.now();
        this.sensorId = sensorId;
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

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
