import express from 'express';
import dotenv from 'dotenv';
import { router } from './routes.js';
import { errorHandler, NotFoundError } from '../shared/errors.js';

dotenv.config();

export const app = express();
const PORT = parseInt(process.env.GESTION_PORT || '3001', 10);

app.use(express.json());

// Health Check
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'GestionService',
    port: PORT,
    timestamp: new Date().toISOString(),
  });
});

// Rutas de negocio
app.use(router);

// 404 Handler
app.use((req, res, next) => {
  next(new NotFoundError(`Ruta no encontrada en GestionService: ${req.method} ${req.originalUrl}`));
});

// Manejo centralizado de errores
app.use(errorHandler);

export function startServer(port = PORT) {
  return new Promise((resolve) => {
    const server = app.listen(port, () => {
      console.log(`[GestionService] Escuchando en el puerto ${port}`);
      resolve(server);
    });
  });
}

if (process.argv[1] && process.argv[1].endsWith('gestion/index.js')) {
  startServer();
}
