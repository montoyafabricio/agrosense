import { z } from 'zod';

// DTO de Entrada: Registro de Medición
// NOTA DE DISEÑO (Limitación del prototipo):
// Este prototipo requiere el registro simultáneo de temperatura y humedad,
// incluso si el sensor físico asociado fue catalogado como de un único tipo.
export const CreateMedicionDTO = z.object({
  temperatura: z.number({
    required_error: 'La temperatura es obligatoria',
    invalid_type_error: 'La temperatura debe ser un número (Double)',
  }).finite('La temperatura debe ser un número finito en grados Celsius'),
  humedad: z.number({
    required_error: 'La humedad es obligatoria',
    invalid_type_error: 'La humedad debe ser un número (Double)',
  }).min(0, 'La humedad relativa no puede ser menor a 0%')
    .max(100, 'La humedad relativa no puede ser superior a 100%'),
  fecha: z.string({
    required_error: 'La fecha y hora de la medición es obligatoria',
  }).datetime({
    offset: true,
    message: 'La fecha debe estar en formato estándar ISO 8601 con zona horaria (ej. 2026-11-05T01:00:00Z)',
  }),
  sensorId: z.string({
    required_error: 'El ID del sensor es obligatorio',
  }).uuid('El sensorId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// Filtros de búsqueda (Query string)
export const MedicionQueryDTO = z.object({
  sensorId: z.string().uuid('El parámetro sensorId debe ser un UUID válido').optional(),
}).strict();

// Parámetro de ruta :id
export const MedicionIdParamDTO = z.object({
  id: z.string().uuid('El ID de la medición debe ser un UUID válido'),
});

// DTO de Salida
export const MedicionOutputDTO = z.object({
  id: z.string().uuid(),
  temperatura: z.number(),
  humedad: z.number(),
  fecha: z.string(),
  sensorId: z.string().uuid(),
});

export function formatMedicionOutput(entity) {
  const fechaIso = entity.fecha instanceof Date 
    ? entity.fecha.toISOString() 
    : new Date(entity.fecha).toISOString();

  return MedicionOutputDTO.parse({
    id: entity.id,
    temperatura: parseFloat(entity.temperatura),
    humedad: parseFloat(entity.humedad),
    fecha: fechaIso,
    sensorId: entity.sensor_id || entity.sensorId,
  });
}
