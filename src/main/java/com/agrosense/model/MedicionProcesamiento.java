package com.agrosense.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mediciones_procesamiento", schema = "mediciones")
public class MedicionProcesamiento {

    @Id
    @Column(name = "medicion_id", nullable = false)
    private UUID medicionId;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "alertas_generadas", columnDefinition = "text")
    private String alertasGeneradas;

    @Column(name = "reintentos", nullable = false)
    private Integer reintentos;

    @Column(name = "ultimo_error", columnDefinition = "text")
    private String ultimoError;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public MedicionProcesamiento() {
    }

    public MedicionProcesamiento(UUID medicionId, String estado, String alertasGeneradas, Integer reintentos, String ultimoError) {
        this.medicionId = medicionId;
        this.estado = estado;
        this.alertasGeneradas = alertasGeneradas != null ? alertasGeneradas : "[]";
        this.reintentos = reintentos != null ? reintentos : 0;
        this.ultimoError = ultimoError;
        this.updatedAt = OffsetDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = OffsetDateTime.now();
        if (this.reintentos == null) {
            this.reintentos = 0;
        }
        if (this.alertasGeneradas == null) {
            this.alertasGeneradas = "[]";
        }
    }

    public UUID getMedicionId() {
        return medicionId;
    }

    public void setMedicionId(UUID medicionId) {
        this.medicionId = medicionId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAlertasGeneradas() {
        return alertasGeneradas;
    }

    public void setAlertasGeneradas(String alertasGeneradas) {
        this.alertasGeneradas = alertasGeneradas;
    }

    public Integer getReintentos() {
        return reintentos;
    }

    public void setReintentos(Integer reintentos) {
        this.reintentos = reintentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
