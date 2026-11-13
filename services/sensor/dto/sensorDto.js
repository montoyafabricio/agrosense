import { z } from 'zod';

export const SensorTipoEnum = z.enum(['humedad', 'temperatura', 'mixto'], {
  errorMap: () => ({ message: "El tipo de sensor debe ser 'humedad', 'temperatura' o 'mixto'" }),
});

export const SensorEstadoEnum = z.enum(['activo', 'inactivo', 'mantenimiento'], {
  errorMap: () => ({ message: "El estado del sensor debe ser 'activo', 'inactivo' o 'mantenimiento'" }),
});

// DTO de Entrada: Creación de Sensor
export const CreateSensorDTO = z.object({
  tipo: SensorTipoEnum,
  ubicacion: z.string({
    required_error: 'La ubicación es obligatoria',
    invalid_type_error: 'La ubicación debe ser una cadena de texto',
  }).trim().min(1, 'La ubicación no puede estar vacía'),
  estado: SensorEstadoEnum,
  cultivoId: z.string({
    required_error: 'El ID del cultivo es obligatorio',
  }).uuid('El cultivoId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// DTO de Entrada: Actualización de Sensor (PUT completo)
export const UpdateSensorDTO = z.object({
  tipo: SensorTipoEnum,
  ubicacion: z.string({
    required_error: 'La ubicación es obligatoria',
    invalid_type_error: 'La ubicación debe ser una cadena de texto',
  }).trim().min(1, 'La ubicación no puede estar vacía'),
  estado: SensorEstadoEnum,
  cultivoId: z.string({
    required_error: 'El ID del cultivo es obligatorio',
  }).uuid('El cultivoId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// Filtros de búsqueda (Query string)
export const SensorQueryDTO = z.object({
  cultivoId: z.string().uuid('El parámetro cultivoId debe ser un UUID válido').optional(),
}).strict();

// Parámetro de ruta :id
export const SensorIdParamDTO = z.object({
  id: z.string().uuid('El ID del sensor debe ser un UUID válido'),
});

// DTO de Salida
export const SensorOutputDTO = z.object({
  id: z.string().uuid(),
  tipo: SensorTipoEnum,
  ubicacion: z.string(),
  estado: SensorEstadoEnum,
  cultivoId: z.string().uuid(),
});

export function formatSensorOutput(entity) {
  return SensorOutputDTO.parse({
    id: entity.id,
    tipo: entity.tipo,
    ubicacion: entity.ubicacion,
    estado: entity.estado,
    cultivoId: entity.cultivo_id || entity.cultivoId,
  });
}
