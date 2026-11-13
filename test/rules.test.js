import test from 'node:test';
import assert from 'node:assert/strict';
import {
  evaluarReglasMedicion,
  REGLA_HUMEDAD_BAJA_UMBRAL,
  REGLA_HUMEDAD_ALTA_UMBRAL,
  REGLA_TEMPERATURA_ALTA_UMBRAL,
} from '../services/medicion/rules.js';

const MOCK_CULTIVO_ID = '11111111-1111-4111-8111-111111111111';
const FECHA_FIJA = '2026-11-13T12:00:00.000Z';

test('Reglas Automáticas - 1. Medición sin condiciones de alerta (valores normales)', () => {
  const alertas = evaluarReglasMedicion(
    { temperatura: 24.0, humedad: 55.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas.length, 0, 'No debería generar alertas con parámetros óptimos');
});

test('Reglas Automáticas - 2. Humedad inferior a 30 (Alerta RIEGO alta)', () => {
  const alertas = evaluarReglasMedicion(
    { temperatura: 22.0, humedad: 28.5 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas.length, 1);
  assert.deepEqual(alertas[0], {
    tipo: 'RIEGO',
    descripcion: 'Revisar la necesidad de riego.',
    prioridad: 'alta',
    fecha: FECHA_FIJA,
    cultivoId: MOCK_CULTIVO_ID,
  });
});

test('Reglas Automáticas - 3. Humedad superior a 80 (Alerta EXCESO_HUMEDAD media)', () => {
  const alertas = evaluarReglasMedicion(
    { temperatura: 20.0, humedad: 85.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas.length, 1);
  assert.deepEqual(alertas[0], {
    tipo: 'EXCESO_HUMEDAD',
    descripcion: 'Revisar el drenaje antes de regar.',
    prioridad: 'media',
    fecha: FECHA_FIJA,
    cultivoId: MOCK_CULTIVO_ID,
  });
});

test('Reglas Automáticas - 4. Temperatura superior a 35 (Alerta CALOR alta)', () => {
  const alertas = evaluarReglasMedicion(
    { temperatura: 36.2, humedad: 60.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas.length, 1);
  assert.deepEqual(alertas[0], {
    tipo: 'CALOR',
    descripcion: 'Revisar las condiciones del cultivo por temperatura elevada.',
    prioridad: 'alta',
    fecha: FECHA_FIJA,
    cultivoId: MOCK_CULTIVO_ID,
  });
});

test('Reglas Automáticas - 5. Humedad baja y temperatura elevada simultáneamente', () => {
  const alertas = evaluarReglasMedicion(
    { temperatura: 38.0, humedad: 20.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas.length, 2, 'Debe generar dos alertas distintas');
  
  const tipos = alertas.map((a) => a.tipo);
  assert.ok(tipos.includes('RIEGO'), 'Debe incluir alerta de RIEGO');
  assert.ok(tipos.includes('CALOR'), 'Debe incluir alerta de CALOR');
});

test('Reglas Automáticas - 6. Valores límite exactos (30, 80 y 35) no generan alertas por comparación estricta', () => {
  // Exactamente 30% de humedad (no es < 30)
  const alertas30 = evaluarReglasMedicion(
    { temperatura: 25.0, humedad: 30.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas30.length, 0, 'Humedad igual a 30 no debe disparar alerta');

  // Exactamente 80% de humedad (no es > 80)
  const alertas80 = evaluarReglasMedicion(
    { temperatura: 25.0, humedad: 80.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas80.length, 0, 'Humedad igual a 80 no debe disparar alerta');

  // Exactamente 35°C de temperatura (no es > 35)
  const alertas35 = evaluarReglasMedicion(
    { temperatura: 35.0, humedad: 50.0 },
    MOCK_CULTIVO_ID,
    FECHA_FIJA
  );
  assert.equal(alertas35.length, 0, 'Temperatura igual a 35 no debe disparar alerta');
});
