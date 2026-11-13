import pg from 'pg';
import dotenv from 'dotenv';

dotenv.config();

const { Pool } = pg;

const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432', 10),
  database: process.env.NODE_ENV === 'test' 
    ? (process.env.DB_NAME_TEST || 'agrosense_test') 
    : (process.env.DB_NAME || 'agrosense'),
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD || '',
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
};

export const pool = new Pool(dbConfig);

pool.on('error', (err) => {
  console.error('[PostgreSQL Pool Error]: Error inesperado en cliente inactivo de base de datos:', err.message);
});

/**
 * Ejecuta una consulta SQL parametrizada utilizando el pool de conexiones
 * @param {string} text - Consulta SQL
 * @param {Array} params - Parámetros de la consulta
 * @returns {Promise<pg.QueryResult>}
 */
export async function query(text, params = []) {
  const start = Date.now();
  try {
    const res = await pool.query(text, params);
    return res;
  } catch (error) {
    console.error(`[SQL Error]: ${error.message} | Query: ${text.substring(0, 100)}...`);
    throw error;
  }
}

/**
 * Obtiene un cliente dedicado del pool para transacciones
 * @returns {Promise<pg.PoolClient>}
 */
export async function getClient() {
  const client = await pool.connect();
  return client;
}

/**
 * Comprueba si la base de datos está disponible
 * @returns {Promise<boolean>}
 */
export async function testConnection() {
  try {
    const client = await pool.connect();
    await client.query('SELECT 1');
    client.release();
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * Cierra todas las conexiones del pool
 */
export async function closePool() {
  await pool.end();
}
