package com.agrosense.service;

import com.agrosense.dto.AlertaCreateDTO;
import com.agrosense.model.AlertaPrioridad;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * AGROSENSE - REGLAS AUTOMATICAS DE AGRICULTURA INTELIGENTE
 * ============================================================================
 *
 * NOTA Y ADVERTENCIA DE DOMINIO:
 * Los umbrales definidos en este modulo son estrictamente demostrativos e ilustrativos
 * para fines academicos. No representan valores agronomicos universales adecuados
 * para todas las especies, etapas de crecimiento o tipos de suelo.
 * El sistema emite recomendaciones informativas y NO controla directamente actuadores,
 * bombas de agua, electrovalvulas ni sistemas fisicos de riego.
 */
@Service
public class AgronomicRulesService {

    public static final double REGLA_HUMEDAD_BAJA_UMBRAL = 30.0;
    public static final double REGLA_HUMEDAD_ALTA_UMBRAL = 80.0;
    public static final double REGLA_TEMPERATURA_ALTA_UMBRAL = 35.0;

    /**
     * Evalua las lecturas de una medicion agronomica y genera las recomendaciones/alertas correspondientes.
     *
     * Reglas evaluadas (con comparacion estricta):
     * 1. Humedad < 30% -> RIEGO (prioridad alta)
     * 2. Humedad > 80% -> EXCESO_HUMEDAD (prioridad media)
     * 3. Temperatura > 35C -> CALOR (prioridad alta)
     */
    public List<AlertaCreateDTO> evaluarReglas(Double temperatura, Double humedad, UUID cultivoId, OffsetDateTime fechaReferencia) {
        List<AlertaCreateDTO> alertas = new ArrayList<>();
        OffsetDateTime fechaActual = fechaReferencia != null ? fechaReferencia : OffsetDateTime.now();

        // Regla 1: Humedad baja (estricta: humedad < 30)
        if (humedad != null && humedad < REGLA_HUMEDAD_BAJA_UMBRAL) {
            alertas.add(new AlertaCreateDTO(
                "RIEGO",
                "Revisar la necesidad de riego.",
                AlertaPrioridad.ALTA,
                fechaActual,
                cultivoId
            ));
        }

        // Regla 2: Humedad elevada (estricta: humedad > 80)
        if (humedad != null && humedad > REGLA_HUMEDAD_ALTA_UMBRAL) {
            alertas.add(new AlertaCreateDTO(
                "EXCESO_HUMEDAD",
                "Revisar el drenaje antes de regar.",
                AlertaPrioridad.MEDIA,
                fechaActual,
                cultivoId
            ));
        }

        // Regla 3: Temperatura elevada (estricta: temperatura > 35)
        if (temperatura != null && temperatura > REGLA_TEMPERATURA_ALTA_UMBRAL) {
            alertas.add(new AlertaCreateDTO(
                "CALOR",
                "Revisar las condiciones del cultivo por temperatura elevada.",
                AlertaPrioridad.ALTA,
                fechaActual,
                cultivoId
            ));
        }

        return alertas;
    }
}
