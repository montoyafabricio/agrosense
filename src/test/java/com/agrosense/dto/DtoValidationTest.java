package com.agrosense.dto;

import com.agrosense.model.AlertaPrioridad;
import com.agrosense.model.SensorEstado;
import com.agrosense.model.SensorTipo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests - Validacion de DTOs")
class DtoValidationTest {

    private static Validator validator;
    private final UUID validUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("1. ProductorCreateDTO con campos obligatorios vacios o nulos es rechazado")
    void testProductorCamposObligatorios() {
        ProductorCreateDTO invalidDto = new ProductorCreateDTO("", " ");
        Set<ConstraintViolation<ProductorCreateDTO>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());

        ProductorCreateDTO validDto = new ProductorCreateDTO("Ana Perez", "ana@example.com");
        Set<ConstraintViolation<ProductorCreateDTO>> validViolations = validator.validate(validDto);
        assertTrue(validViolations.isEmpty());
    }

    @Test
    @DisplayName("2. CultivoCreateDTO con superficie <= 0 o campos nulos es rechazado")
    void testCultivoSuperficieInvalida() {
        CultivoCreateDTO invalidDto = new CultivoCreateDTO(
            "Soja",
            -5.0,
            LocalDate.of(2026, 10, 10),
            validUUID
        );
        Set<ConstraintViolation<CultivoCreateDTO>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("superficie")));

        CultivoCreateDTO zeroSuperficie = new CultivoCreateDTO(
            "Soja",
            0.0,
            LocalDate.of(2026, 10, 10),
            validUUID
        );
        assertFalse(validator.validate(zeroSuperficie).isEmpty());

        CultivoCreateDTO validDto = new CultivoCreateDTO(
            "Soja",
            10.5,
            LocalDate.of(2026, 10, 10),
            validUUID
        );
        assertTrue(validator.validate(validDto).isEmpty());
    }

    @Test
    @DisplayName("3. MedicionCreateDTO con humedad fuera de rango (0 - 100) es rechazada")
    void testMedicionHumedadFueraDeRango() {
        MedicionCreateDTO humedadNegativa = new MedicionCreateDTO(
            25.0,
            -5.0,
            OffsetDateTime.now(),
            validUUID
        );
        Set<ConstraintViolation<MedicionCreateDTO>> negViolations = validator.validate(humedadNegativa);
        assertFalse(negViolations.isEmpty());

        MedicionCreateDTO humedadExcesiva = new MedicionCreateDTO(
            25.0,
            105.0,
            OffsetDateTime.now(),
            validUUID
        );
        Set<ConstraintViolation<MedicionCreateDTO>> excViolations = validator.validate(humedadExcesiva);
        assertFalse(excViolations.isEmpty());

        MedicionCreateDTO humedadValida = new MedicionCreateDTO(
            25.0,
            75.0,
            OffsetDateTime.now(),
            validUUID
        );
        assertTrue(validator.validate(humedadValida).isEmpty());
    }

    @Test
    @DisplayName("4. SensorCreateDTO con tipo o estado nulo es rechazado")
    void testSensorCamposRequeridos() {
        SensorCreateDTO invalidDto = new SensorCreateDTO(
            null,
            "Lote Norte",
            null,
            validUUID
        );
        Set<ConstraintViolation<SensorCreateDTO>> violations = validator.validate(invalidDto);
        assertEquals(2, violations.size());

        SensorCreateDTO validDto = new SensorCreateDTO(
            SensorTipo.HUMEDAD,
            "Lote Norte",
            SensorEstado.ACTIVO,
            validUUID
        );
        assertTrue(validator.validate(validDto).isEmpty());
    }

    @Test
    @DisplayName("5. AlertaCreateDTO con campos en blanco o nulos es rechazada")
    void testAlertaValidaciones() {
        AlertaCreateDTO invalidDto = new AlertaCreateDTO(
            "",
            "   ",
            null,
            null,
            null
        );
        Set<ConstraintViolation<AlertaCreateDTO>> violations = validator.validate(invalidDto);
        assertEquals(5, violations.size());

        AlertaCreateDTO validDto = new AlertaCreateDTO(
            "RIEGO",
            "Revisar la necesidad de riego.",
            AlertaPrioridad.ALTA,
            OffsetDateTime.now(),
            validUUID
        );
        assertTrue(validator.validate(validDto).isEmpty());
    }
}
