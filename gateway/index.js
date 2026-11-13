import express from 'express';
import dotenv from 'dotenv';
import { router } from './routes.js';
import { errorHandler, NotFoundError } from '../services/shared/errors.js';

dotenv.config();

export const app = express();
const PORT = parseInt(process.env.GATEWAY_PORT || '8080', 10);

app.use(express.json());

// Health Check del Gateway y de la topología de Microservicios
app.get('/health', async (req, res) => {
  const GESTION_URL = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';
  const SENSOR_URL = process.env.SENSOR_SERVICE_URL || 'http://localhost:3002';
  const MEDICION_URL = process.env.MEDICION_SERVICE_URL || 'http://localhost:3003';
  const ALERTA_URL = process.env.ALERTA_SERVICE_URL || 'http://localhost:3004';

  async function checkService(url) {
    try {
      const response = await fetch(`${url}/health`, { signal: AbortSignal.timeout(1500) });
      return response.ok ? 'UP' : 'DOWN';
    } catch {
      return 'DOWN';
    }
  }

  const [gestion, sensor, medicion, alerta] = await Promise.all([
    checkService(GESTION_URL),
    checkService(SENSOR_URL),
    checkService(MEDICION_URL),
    checkService(ALERTA_URL),
  ]);

  const allUp = [gestion, sensor, medicion, alerta].every((s) => s === 'UP');

  res.status(allUp ? 200 : 207).json({
    status: allUp ? 'UP' : 'DEGRADED',
    gateway: {
      name: 'AgroSense API Gateway',
      port: PORT,
      status: 'UP',
    },
    microservices: {
      gestionService: { url: GESTION_URL, status: gestion },
      sensorService: { url: SENSOR_URL, status: sensor },
      medicionService: { url: MEDICION_URL, status: medicion },
      alertaService: { url: ALERTA_URL, status: alerta },
    },
    timestamp: new Date().toISOString(),
  });
});

// Enrutador del Gateway
app.use(router);

// 404 Handler para endpoints no reconocidos
app.use((req, res, next) => {
  next(new NotFoundError(`Ruta no encontrada en el API Gateway: ${req.method} ${req.originalUrl}`));
});

// Middleware de manejo de errores
app.use(errorHandler);

export function startServer(port = PORT) {
  return new Promise((resolve) => {
    const server = app.listen(port, () => {
      console.log(`[API Gateway] Escuchando solicitudes públicas en http://localhost:${port}`);
      resolve(server);
    });
  });
}

if (process.argv[1] && process.argv[1].endsWith('gateway/index.js')) {
  startServer();
}
