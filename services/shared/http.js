import { ServiceUnavailableError, AppError } from './errors.js';

const DEFAULT_TIMEOUT_MS = 5000;

/**
 * Cliente HTTP para comunicación entre microservicios usando fetch nativo de Node.js
 * @param {string} url - URL de destino
 * @param {object} options - Opciones de fetch (method, body, headers, etc.)
 * @param {string} serviceName - Nombre legible del servicio para logs y errores 503
 * @returns {Promise<any>}
 */
export async function interServiceRequest(url, options = {}, serviceName = 'Servicio Externo') {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), options.timeoutMs || DEFAULT_TIMEOUT_MS);

  const config = {
    ...options,
    signal: controller.signal,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {}),
    },
  };

  if (options.body && typeof options.body === 'object') {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(url, config);
    clearTimeout(timeoutId);

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
      const message = (data && data.error && data.error.message) || `Error HTTP ${response.status} en ${serviceName}`;
      throw new AppError(message, response.status, data?.error?.details || null);
    }

    return data;
  } catch (error) {
    clearTimeout(timeoutId);

    if (error instanceof AppError) {
      throw error;
    }

    // Errores de red, conexión rechazada o timeout
    if (error.name === 'AbortError' || error.code === 'ECONNREFUSED' || error.cause?.code === 'ECONNREFUSED') {
      throw new ServiceUnavailableError(serviceName);
    }

    throw new ServiceUnavailableError(`${serviceName} (${error.message})`);
  }
}
