import crypto from 'node:crypto';
import { query } from '../../../database/connection.js';

export class MedicionRepository {
  async findAll(filter = {}) {
    let sql = 'SELECT id, temperatura, humedad, fecha, sensor_id FROM mediciones.mediciones';
    const params = [];

    if (filter.sensorId) {
      params.push(filter.sensorId);
      sql += ' WHERE sensor_id = $1';
    }

    sql += ' ORDER BY fecha DESC';

    const res = await query(sql, params);
    return res.rows;
  }

  async findById(id) {
    const res = await query(
      'SELECT id, temperatura, humedad, fecha, sensor_id FROM mediciones.mediciones WHERE id = $1',
      [id]
    );
    return res.rows[0] || null;
  }

  async create({ temperatura, humedad, fecha, sensorId }) {
    const id = crypto.randomUUID();
    const res = await query(
      `INSERT INTO mediciones.mediciones (id, temperatura, humedad, fecha, sensor_id) 
       VALUES ($1, $2, $3, $4, $5) 
       RETURNING id, temperatura, humedad, fecha, sensor_id`,
      [id, temperatura, humedad, fecha, sensorId]
    );
    return res.rows[0];
  }

  // --- Métodos de control técnico para Idempotencia y Reintentos ---
  async saveProcessingStatus(medicionId, { estado, alertasGeneradas = [], error = null }) {
    const res = await query(
      `INSERT INTO mediciones.mediciones_procesamiento (medicion_id, estado, alertas_generadas, ultimo_error, updated_at) 
       VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP) 
       ON CONFLICT (medicion_id) 
       DO UPDATE SET 
         estado = EXCLUDED.estado, 
         alertas_generadas = EXCLUDED.alertas_generadas, 
         reintentos = mediciones.mediciones_procesamiento.reintentos + 1, 
         ultimo_error = EXCLUDED.ultimo_error, 
         updated_at = CURRENT_TIMESTAMP 
       RETURNING *`,
      [medicionId, estado, JSON.stringify(alertasGeneradas), error]
    );
    return res.rows[0];
  }

  async getProcessingStatus(medicionId) {
    const res = await query(
      'SELECT medicion_id, estado, alertas_generadas, reintentos, ultimo_error FROM mediciones.mediciones_procesamiento WHERE medicion_id = $1',
      [medicionId]
    );
    return res.rows[0] || null;
  }
}

export const medicionRepository = new MedicionRepository();
