package com.agrosense.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas", schema = "alertas")
public class Alerta {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private AlertaPrioridad prioridad;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha;

    @Column(name = "cultivo_id", nullable = false)
    private UUID cultivoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Alerta() {
    }

    public Alerta(UUID id, String tipo, String descripcion, AlertaPrioridad prioridad, OffsetDateTime fecha, UUID cultivoId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fecha = fecha != null ? fecha : OffsetDateTime.now();
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public AlertaPrioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(AlertaPrioridad prioridad) {
        this.prioridad = prioridad;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
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
