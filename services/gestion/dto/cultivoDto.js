import { z } from 'zod';

const dateRegex = /^\d{4}-\d{2}-\d{2}$/;

// DTO de Entrada: Creación de Cultivo
export const CreateCultivoDTO = z.object({
  especie: z.string({
    required_error: 'La especie es obligatoria',
    invalid_type_error: 'La especie debe ser una cadena de texto',
  }).trim().min(1, 'La especie no puede estar vacía'),
  superficie: z.number({
    required_error: 'La superficie es obligatoria',
    invalid_type_error: 'La superficie debe ser un valor numérico (Double)',
  }).positive('La superficie debe ser un número positivo mayor a 0 (hectáreas)'),
  fechaSiembra: z.string({
    required_error: 'La fecha de siembra es obligatoria',
  }).regex(dateRegex, 'La fecha de siembra debe tener formato YYYY-MM-DD')
    .refine((dateStr) => !isNaN(Date.parse(dateStr)), {
      message: 'La fecha de siembra debe ser una fecha de calendario válida',
    }),
  productorId: z.string({
    required_error: 'El ID del productor es obligatorio',
  }).uuid('El productorId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// DTO de Entrada: Actualización completa de Cultivo (PUT)
export const UpdateCultivoDTO = z.object({
  especie: z.string({
    required_error: 'La especie es obligatoria',
    invalid_type_error: 'La especie debe ser una cadena de texto',
  }).trim().min(1, 'La especie no puede estar vacía'),
  superficie: z.number({
    required_error: 'La superficie es obligatoria',
    invalid_type_error: 'La superficie debe ser un valor numérico (Double)',
  }).positive('La superficie debe ser un número positivo mayor a 0 (hectáreas)'),
  fechaSiembra: z.string({
    required_error: 'La fecha de siembra es obligatoria',
  }).regex(dateRegex, 'La fecha de siembra debe tener formato YYYY-MM-DD')
    .refine((dateStr) => !isNaN(Date.parse(dateStr)), {
      message: 'La fecha de siembra debe ser una fecha de calendario válida',
    }),
  productorId: z.string({
    required_error: 'El ID del productor es obligatorio',
  }).uuid('El productorId debe ser un UUID válido'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// Parámetro de ruta :id
export const CultivoIdParamDTO = z.object({
  id: z.string().uuid('El ID del cultivo debe ser un UUID válido'),
});

// DTO de Salida
export const CultivoOutputDTO = z.object({
  id: z.string().uuid(),
  especie: z.string(),
  superficie: z.number(),
  fechaSiembra: z.string(),
  productorId: z.string().uuid(),
});

export function formatCultivoOutput(entity) {
  // Normalizar fechaSiembra a YYYY-MM-DD si viene como Date de PostgreSQL
  const fechaStr = entity.fecha_siembra instanceof Date 
    ? entity.fecha_siembra.toISOString().split('T')[0] 
    : (entity.fecha_siembra || entity.fechaSiembra);

  return CultivoOutputDTO.parse({
    id: entity.id,
    especie: entity.especie,
    superficie: parseFloat(entity.superficie),
    fechaSiembra: String(fechaStr).substring(0, 10),
    productorId: entity.productor_id || entity.productorId,
  });
}
