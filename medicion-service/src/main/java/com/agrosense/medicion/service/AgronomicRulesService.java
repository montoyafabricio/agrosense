package com.agrosense.medicion.service;

import com.agrosense.medicion.dto.AlertaCreateDTO;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * AGROSENSE - REGLAS AUTOMATICAS DE AGRICULTURA INTELIGENTE
 * ============================================================================
 * Evalua lecturas agronomicas y genera alertas cuando se detectan anomalias.
 * Umbrales utilizados con fines academicos y demostrativos.
 */
@Service
public class AgronomicRulesService {

    public static final double REGLA_HUMEDAD_BAJA_UMBRAL = 30.0;
    public static final double REGLA_HUMEDAD_ALTA_UMBRAL = 80.0;
    public static final double REGLA_TEMPERATURA_ALTA_UMBRAL = 35.0;

    /**
     * Evalua las lecturas y genera AlertaCreateDTO para enviar al alerta-service.
     * Reglas:
     * 1. Humedad < 30 -> RIEGO (ALTA)
     * 2. Humedad > 80 -> EXCESO_HUMEDAD (MEDIA)
     * 3. Temperatura > 35 -> CALOR (ALTA)
     */
    public List<AlertaCreateDTO> evaluarReglas(Double temperatura, Double humedad, UUID cultivoId, OffsetDateTime fechaReferencia) {
        List<AlertaCreateDTO> alertas = new ArrayList<>();
        OffsetDateTime fecha = fechaReferencia != null ? fechaReferencia : OffsetDateTime.now();

        if (humedad != null && humedad < REGLA_HUMEDAD_BAJA_UMBRAL) {
            alertas.add(new AlertaCreateDTO("RIEGO", "Revisar la necesidad de riego.", "ALTA", fecha, cultivoId));
        }
        if (humedad != null && humedad > REGLA_HUMEDAD_ALTA_UMBRAL) {
            alertas.add(new AlertaCreateDTO("EXCESO_HUMEDAD", "Revisar el drenaje antes de regar.", "MEDIA", fecha, cultivoId));
        }
        if (temperatura != null && temperatura > REGLA_TEMPERATURA_ALTA_UMBRAL) {
            alertas.add(new AlertaCreateDTO("CALOR", "Revisar las condiciones del cultivo por temperatura elevada.", "ALTA", fecha, cultivoId));
        }

        return alertas;
    }
}
