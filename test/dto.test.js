import test from 'node:test';
import assert from 'node:assert/strict';
import { CreateProductorDTO } from '../services/gestion/dto/productorDto.js';
import { CreateCultivoDTO } from '../services/gestion/dto/cultivoDto.js';
import { CreateSensorDTO } from '../services/sensor/dto/sensorDto.js';
import { CreateMedicionDTO } from '../services/medicion/dto/medicionDto.js';
import { CreateAlertaDTO } from '../services/alerta/dto/alertaDto.js';

const VALID_UUID = '123e4567-e89b-12d3-a456-426614174000';

test('DTO Validaciones - 1. Campos obligatorios ausentes son rechazados', () => {
  assert.throws(() => {
    CreateProductorDTO.parse({ nombre: 'Juan' }); // Falta contacto
  }, /contacto/);

  assert.throws(() => {
    CreateCultivoDTO.parse({ especie: 'Trigo' }); // Faltan superficie, fechaSiembra, productorId
  });

  assert.throws(() => {
    CreateSensorDTO.parse({ tipo: 'humedad' }); // Faltan ubicacion, estado, cultivoId
  });

  assert.throws(() => {
    CreateMedicionDTO.parse({ temperatura: 25 }); // Faltan humedad, fecha, sensorId
  });

  assert.throws(() => {
    CreateAlertaDTO.parse({ tipo: 'RIEGO' }); // Faltan descripcion, prioridad, fecha, cultivoId
  });
});

test('DTO Validaciones - 2. UUID inválido es rechazado', () => {
  assert.throws(() => {
    CreateCultivoDTO.parse({
      especie: 'Soja',
      superficie: 10,
      fechaSiembra: '2026-10-10',
      productorId: 'no-es-un-uuid',
    });
  }, /UUID/i);

  assert.throws(() => {
    CreateSensorDTO.parse({
      tipo: 'temperatura',
      ubicacion: 'Lote 1',
      estado: 'activo',
      cultivoId: '12345',
    });
  }, /UUID/i);
});

test('DTO Validaciones - 3. Humedad fuera de rango (0 - 100) es rechazada', () => {
  // Humedad negativa
  assert.throws(() => {
    CreateMedicionDTO.parse({
      temperatura: 25.0,
      humedad: -5.0,
      fecha: '2026-11-13T10:00:00.000Z',
      sensorId: VALID_UUID,
    });
  }, /humedad/i);

  // Humedad mayor a 100
  assert.throws(() => {
    CreateMedicionDTO.parse({
      temperatura: 25.0,
      humedad: 105.0,
      fecha: '2026-11-13T10:00:00.000Z',
      sensorId: VALID_UUID,
    });
  }, /humedad/i);

  // Humedad válida dentro de rango
  const valid = CreateMedicionDTO.parse({
    temperatura: 25.0,
    humedad: 75.0,
    fecha: '2026-11-13T10:00:00.000Z',
    sensorId: VALID_UUID,
  });
  assert.equal(valid.humedad, 75.0);
});

test('DTO Validaciones - 4. Prioridad inválida en Alerta es rechazada', () => {
  assert.throws(() => {
    CreateAlertaDTO.parse({
      tipo: 'RIEGO',
      descripcion: 'Revisar riego',
      prioridad: 'urgente_maxima', // Inválida, solo baja/media/alta
      fecha: '2026-11-13T10:00:00.000Z',
      cultivoId: VALID_UUID,
    });
  }, /prioridad/i);
});

test('DTO Validaciones - 5. Estado inválido en Sensor es rechazado', () => {
  assert.throws(() => {
    CreateSensorDTO.parse({
      tipo: 'mixto',
      ubicacion: 'Invernadero 2',
      estado: 'roto', // Inválido, solo activo/inactivo/mantenimiento
      cultivoId: VALID_UUID,
    });
  }, /estado/i);
});

test('DTO Validaciones - 6. Campos desconocidos son rechazados (.strict())', () => {
  assert.throws(() => {
    CreateProductorDTO.parse({
      nombre: 'Carlos',
      contacto: 'carlos@campo.com',
      campoInfiltrado: 'hack',
    });
  }, /adicionales/i);
});

test('DTO Validaciones - 7. Intento de asignar manualmente un ID al crear es rechazado', () => {
  assert.throws(() => {
    CreateProductorDTO.parse({
      id: VALID_UUID, // El cliente NO puede enviar ID
      nombre: 'Lucía',
      contacto: 'lucia@campo.com',
    });
  }, /adicionales/i);

  assert.throws(() => {
    CreateSensorDTO.parse({
      id: VALID_UUID,
      tipo: 'temperatura',
      ubicacion: 'Nave Central',
      estado: 'activo',
      cultivoId: VALID_UUID,
    });
  }, /adicionales/i);
});
