import { z } from 'zod';

// DTO de Entrada: Creación de Productor
// El cliente NO puede enviar ID y se rechazan campos desconocidos (.strict())
export const CreateProductorDTO = z.object({
  nombre: z.string({
    required_error: 'El nombre es obligatorio',
    invalid_type_error: 'El nombre debe ser una cadena de texto',
  }).trim().min(1, 'El nombre no puede estar vacío'),
  contacto: z.string({
    required_error: 'El contacto es obligatorio',
    invalid_type_error: 'El contacto debe ser una cadena de texto',
  }).trim().min(1, 'El contacto no puede estar vacío'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// DTO de Entrada: Actualización de Productor (PUT completo)
export const UpdateProductorDTO = z.object({
  nombre: z.string({
    required_error: 'El nombre es obligatorio',
    invalid_type_error: 'El nombre debe ser una cadena de texto',
  }).trim().min(1, 'El nombre no puede estar vacío'),
  contacto: z.string({
    required_error: 'El contacto es obligatorio',
    invalid_type_error: 'El contacto debe ser una cadena de texto',
  }).trim().min(1, 'El contacto no puede estar vacío'),
}).strict({
  message: 'No se permiten campos adicionales no definidos en la entidad',
});

// Parámetro de ruta :id
export const ProductorIdParamDTO = z.object({
  id: z.string().uuid('El ID del productor debe ser un UUID válido'),
});

// DTO de Salida: Controla los campos exactos devueltos al cliente
export const ProductorOutputDTO = z.object({
  id: z.string().uuid(),
  nombre: z.string(),
  contacto: z.string(),
});

export function formatProductorOutput(entity) {
  return ProductorOutputDTO.parse({
    id: entity.id,
    nombre: entity.nombre,
    contacto: entity.contacto,
  });
}
