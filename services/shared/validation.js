import { ZodError } from 'zod';
import { ValidationError } from './errors.js';

/**
 * Middleware para validar el cuerpo de una petición HTTP con un esquema Zod
 * @param {import('zod').ZodSchema} schema
 */
export function validateBody(schema) {
  return (req, res, next) => {
    try {
      req.body = schema.parse(req.body);
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const details = error.errors.map((err) => ({
          field: err.path.join('.'),
          message: err.message,
          rule: err.code,
        }));
        return next(new ValidationError('Error de validación en los datos enviados', details));
      }
      next(error);
    }
  };
}

/**
 * Middleware para validar los parámetros de consulta (query string)
 * @param {import('zod').ZodSchema} schema
 */
export function validateQuery(schema) {
  return (req, res, next) => {
    try {
      req.query = schema.parse(req.query);
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const details = error.errors.map((err) => ({
          field: err.path.join('.'),
          message: err.message,
        }));
        return next(new ValidationError('Parámetros de búsqueda inválidos', details));
      }
      next(error);
    }
  };
}

/**
 * Middleware para validar los parámetros de ruta (ej. :id)
 * @param {import('zod').ZodSchema} schema
 */
export function validateParams(schema) {
  return (req, res, next) => {
    try {
      req.params = schema.parse(req.params);
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const details = error.errors.map((err) => ({
          field: err.path.join('.'),
          message: err.message,
        }));
        return next(new ValidationError('Parámetros de ruta inválidos', details));
      }
      next(error);
    }
  };
}
