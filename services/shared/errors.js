export class AppError extends Error {
  constructor(message, statusCode = 500, details = null) {
    super(message);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
    this.details = details;
    Error.captureStackTrace(this, this.constructor);
  }
}

export class ValidationError extends AppError {
  constructor(message = 'Datos de solicitud inválidos', details = null) {
    super(message, 400, details);
  }
}

export class NotFoundError extends AppError {
  constructor(message = 'Recurso no encontrado') {
    super(message, 404);
  }
}

export class ConflictError extends AppError {
  constructor(message = 'Conflicto con el estado actual del recurso') {
    super(message, 409);
  }
}

export class ServiceUnavailableError extends AppError {
  constructor(serviceName = 'Servicio dependiente') {
    super(`El servicio ${serviceName} no se encuentra disponible`, 503);
  }
}

/**
 * Middleware centralizado de manejo de errores para Express
 */
export function errorHandler(err, req, res, next) {
  const statusCode = err.statusCode || 500;
  const isInternal = statusCode === 500;

  if (isInternal) {
    console.error(`[Error No Controlado] ${req.method} ${req.url}:`, err);
  }

  const response = {
    error: {
      code: err.name || 'InternalServerError',
      message: isInternal ? 'Ha ocurrido un error interno en el servidor' : err.message,
      ...(err.details && { details: err.details }),
      timestamp: new Date().toISOString(),
      path: req.originalUrl || req.url,
    },
  };

  res.status(statusCode).json(response);
}
