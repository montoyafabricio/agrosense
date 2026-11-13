import { Router } from 'express';
import dotenv from 'dotenv';

dotenv.config();

export const router = Router();

const GESTION_URL = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';
const SENSOR_URL = process.env.SENSOR_SERVICE_URL || 'http://localhost:3002';
const MEDICION_URL = process.env.MEDICION_SERVICE_URL || 'http://localhost:3003';
const ALERTA_URL = process.env.ALERTA_SERVICE_URL || 'http://localhost:3004';

/**
 * Función proxy genérica que reenvía peticiones HTTP al microservicio correspondiente
 * Preserva método, URL completa con query strings y cuerpo JSON sanitizado.
 */
function createProxyHandler(targetBaseUrl, serviceName) {
  return async (req, res) => {
    const targetUrl = `${targetBaseUrl}${req.originalUrl}`;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 6000);

    // Evita duplicidad de headers (ej. content-type y Content-Type) y problemas de hop-by-hop
    const forwardHeaders = {
      'content-type': 'application/json',
      'accept': 'application/json',
    };

    if (req.headers['x-idempotency-key']) {
      forwardHeaders['x-idempotency-key'] = req.headers['x-idempotency-key'];
    }

    const fetchOptions = {
      method: req.method,
      headers: forwardHeaders,
      signal: controller.signal,
    };

    if (['POST', 'PUT', 'PATCH'].includes(req.method)) {
      fetchOptions.body = JSON.stringify(req.body ?? {});
    }

    try {
      const response = await fetch(targetUrl, fetchOptions);
      clearTimeout(timeout);

      const contentType = response.headers.get('content-type') || '';
      res.status(response.status);

      if (response.status === 204) {
        return res.send();
      }

      if (contentType.includes('application/json')) {
        const json = await response.json();
        return res.json(json);
      } else {
        const text = await response.text();
        return res.send(text);
      }
    } catch (error) {
      clearTimeout(timeout);
      console.error(`[API Gateway] Error comunicando con ${serviceName} (${targetUrl}): ${error.message}`);

      // Código 503 cuando el microservicio destino no está disponible
      return res.status(503).json({
        error: {
          code: 'ServiceUnavailable',
          message: `El microservicio ${serviceName} no se encuentra disponible o no respondió a tiempo`,
          details: error.name === 'AbortError' ? 'Tiempo de espera agotado (timeout)' : error.message,
          timestamp: new Date().toISOString(),
          path: req.originalUrl,
        },
      });
    }
  };
}

// ================= Enrutamiento a Microservicios =================

// 1. Gestión (Productores y Cultivos) -> GestionService (:3001)
router.use('/api/productores', createProxyHandler(GESTION_URL, 'GestionService'));
router.use('/api/cultivos', createProxyHandler(GESTION_URL, 'GestionService'));

// 2. Sensores -> SensorService (:3002)
router.use('/api/sensores', createProxyHandler(SENSOR_URL, 'SensorService'));

// 3. Mediciones -> MedicionService (:3003)
router.use('/api/mediciones', createProxyHandler(MEDICION_URL, 'MedicionService'));

// 4. Alertas -> AlertaService (:3004)
router.use('/api/alertas', createProxyHandler(ALERTA_URL, 'AlertaService'));
