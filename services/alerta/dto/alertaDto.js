import { z } from 'zod';

export const AlertaPrioridadEnum = z.enum(['baja', 'media', 'alta'], {
  errorMap: () => ({ message: "La prioridad de la alerta debe ser 'baja', 'media' o 'alta'" }),
});

// DTO de Entrada: Creación de Alerta
// IMPORTANTE: Alerta NO tiene el atributo medicionId, respetando fielmente el diagrama de clases original.
export const CreateAlertaDTO = z.object({
  tipo: z.string({
    required_error: 'El tipo de alerta es obligatorio',
    invalid_type_error: 'El tipo debe ser una cadena de texto',
  }).trim().min(1, 'El tipo de alerta no puede estar vacío'),
  descripcion: z.string({
    required_error: 'La descripción es obligatoria',
    invalid_type_error: 'La descripción debe ser una cadena de texto',
  }).trim().min(1, 'La descripción no puede estar vacía'),
  prioridad: AlertaPrioridadEnum,
  fecha: z.string({
    required_error: 'La fecha de la alerta es obligatoria',
  }).datetime({
    message: 'La fecha debe estar en formato estándar ISO 8601',
  }),
  cultivoId: z.string({
    required_error: 'El ID del cultivo es obligatorio',
  }).uuid('El cultivoId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad (Alerta no admite medicionId)',
});

// Filtros de búsqueda (Query string)
export const AlertaQueryDTO = z.object({
  cultivoId: z.string().uuid('El parámetro cultivoId debe ser un UUID válido').optional(),
  prioridad: AlertaPrioridadEnum.optional(),
}).strict();

// Parámetro de ruta :id
export const AlertaIdParamDTO = z.object({
  id: z.string().uuid('El ID de la alerta debe ser un UUID válido'),
});

// DTO de Salida
export const AlertaOutputDTO = z.object({
  id: z.string().uuid(),
  tipo: z.string(),
  descripcion: z.string(),
  prioridad: AlertaPrioridadEnum,
  fecha: z.string(),
  cultivoId: z.string().uuid(),
});

export function formatAlertaOutput(entity) {
  const fechaIso = entity.fecha instanceof Date 
    ? entity.fecha.toISOString() 
    : new Date(entity.fecha).toISOString();

  return AlertaOutputDTO.parse({
    id: entity.id,
    tipo: entity.tipo,
    descripcion: entity.descripcion,
    prioridad: entity.prioridad,
    fecha: fechaIso,
    cultivoId: entity.cultivo_id || entity.cultivoId,
  });
}
