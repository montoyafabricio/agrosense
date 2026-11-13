import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { pool, testConnection } from './connection.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export async function initializeDatabase() {
  const isConnected = await testConnection();
  if (!isConnected) {
    throw new Error(
      `No se pudo conectar a PostgreSQL en ${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || '5432'}.\n` +
      `Por favor verifica que el servicio de PostgreSQL esté activo y que la base de datos "${process.env.DB_NAME || 'agrosense'}" exista.`
    );
  }

  const sqlPath = path.join(__dirname, 'init.sql');
  const sql = fs.readFileSync(sqlPath, 'utf-8');

  console.log('[Database]: Inicializando esquemas y tablas de AgroSense...');
  await pool.query(sql);
  console.log('[Database]: Esquemas, tablas e índices inicializados correctamente (idempotente).');
}

// Si se ejecuta directamente con node database/init.js
if (process.argv[1] && process.argv[1].endsWith('init.js')) {
  initializeDatabase()
    .then(async () => {
      await pool.end();
      process.exit(0);
    })
    .catch(async (err) => {
      console.error('[Database Error]:', err.message);
      await pool.end();
      process.exit(1);
    });
}
