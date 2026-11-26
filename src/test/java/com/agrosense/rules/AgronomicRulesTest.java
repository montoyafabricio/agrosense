package com.agrosense.rules;

import com.agrosense.dto.AlertaCreateDTO;
import com.agrosense.model.AlertaPrioridad;
import com.agrosense.service.AgronomicRulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests - Reglas Agronomicas Automaticas")
class AgronomicRulesTest {

    private AgronomicRulesService rulesService;
    private final UUID mockCultivoId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private final OffsetDateTime fechaFija = OffsetDateTime.parse("2026-11-13T12:00:00Z");

    @BeforeEach
    void setUp() {
        rulesService = new AgronomicRulesService();
    }

    @Test
    @DisplayName("1. Medicion sin condiciones de alerta (valores optimos)")
    void testParametrosNormalesNoGeneranAlertas() {
        List<AlertaCreateDTO> alertas = rulesService.evaluarReglas(24.0, 55.0, mockCultivoId, fechaFija);
        assertTrue(alertas.isEmpty(), "No deberia generar alertas con parametros optimos");
    }

    @Test
    @DisplayName("2. Humedad inferior a 30% dispara Alerta RIEGO alta")
    void testHumedadBajaDisparaRiego() {
        List<AlertaCreateDTO> alertas = rulesService.evaluarReglas(22.0, 28.5, mockCultivoId, fechaFija);
        assertEquals(1, alertas.size());
        AlertaCreateDTO alerta = alertas.getFirst();
        assertEquals("RIEGO", alerta.tipo());
        assertEquals("Revisar la necesidad de riego.", alerta.descripcion());
        assertEquals(AlertaPrioridad.ALTA, alerta.prioridad());
        assertEquals(mockCultivoId, alerta.cultivoId());
    }

    @Test
    @DisplayName("3. Humedad superior a 80% dispara Alerta EXCESO_HUMEDAD media")
    void testHumedadAltaDisparaExcesoHumedad() {
        List<AlertaCreateDTO> alertas = rulesService.evaluarReglas(20.0, 85.0, mockCultivoId, fechaFija);
        assertEquals(1, alertas.size());
        AlertaCreateDTO alerta = alertas.getFirst();
        assertEquals("EXCESO_HUMEDAD", alerta.tipo());
        assertEquals("Revisar el drenaje antes de regar.", alerta.descripcion());
        assertEquals(AlertaPrioridad.MEDIA, alerta.prioridad());
        assertEquals(mockCultivoId, alerta.cultivoId());
    }

    @Test
    @DisplayName("4. Temperatura superior a 35C dispara Alerta CALOR alta")
    void testTemperaturaAltaDisparaCalor() {
        List<AlertaCreateDTO> alertas = rulesService.evaluarReglas(36.2, 60.0, mockCultivoId, fechaFija);
        assertEquals(1, alertas.size());
        AlertaCreateDTO alerta = alertas.getFirst();
        assertEquals("CALOR", alerta.tipo());
        assertEquals("Revisar las condiciones del cultivo por temperatura elevada.", alerta.descripcion());
        assertEquals(AlertaPrioridad.ALTA, alerta.prioridad());
        assertEquals(mockCultivoId, alerta.cultivoId());
    }

    @Test
    @DisplayName("5. Humedad baja y temperatura elevada simultaneamente disparan dos alertas")
    void testCondicionesMultiplesDisparanMultiplesAlertas() {
        List<AlertaCreateDTO> alertas = rulesService.evaluarReglas(38.0, 20.0, mockCultivoId, fechaFija);
        assertEquals(2, alertas.size(), "Debe generar dos alertas distintas");

        List<String> tipos = alertas.stream().map(AlertaCreateDTO::tipo).toList();
        assertTrue(tipos.contains("RIEGO"), "Debe incluir alerta de RIEGO");
        assertTrue(tipos.contains("CALOR"), "Debe incluir alerta de CALOR");
    }

    @Test
    @DisplayName("6. Valores limite exactos (30, 80 y 35) no generan alertas por comparacion estricta")
    void testValoresLimiteNoDisparanAlertas() {
        List<AlertaCreateDTO> alertasHumedad30 = rulesService.evaluarReglas(25.0, 30.0, mockCultivoId, fechaFija);
        assertTrue(alertasHumedad30.isEmpty(), "Humedad igual a 30 no debe disparar alerta");

        List<AlertaCreateDTO> alertasHumedad80 = rulesService.evaluarReglas(25.0, 80.0, mockCultivoId, fechaFija);
        assertTrue(alertasHumedad80.isEmpty(), "Humedad igual a 80 no debe disparar alerta");

        List<AlertaCreateDTO> alertasTemp35 = rulesService.evaluarReglas(35.0, 50.0, mockCultivoId, fechaFija);
        assertTrue(alertasTemp35.isEmpty(), "Temperatura igual a 35 no debe disparar alerta");
    }
}
