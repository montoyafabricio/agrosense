import crypto from 'node:crypto';
import { query } from '../../../database/connection.js';

export class SensorRepository {
  async findAll(filter = {}) {
    let sql = 'SELECT id, tipo, ubicacion, estado, cultivo_id FROM sensores.sensores';
    const params = [];

    if (filter.cultivoId) {
      params.push(filter.cultivoId);
      sql += ' WHERE cultivo_id = $1';
    }

    sql += ' ORDER BY created_at ASC';

    const res = await query(sql, params);
    return res.rows;
  }

  async findById(id) {
    const res = await query(
      'SELECT id, tipo, ubicacion, estado, cultivo_id FROM sensores.sensores WHERE id = $1',
      [id]
    );
    return res.rows[0] || null;
  }

  async create({ tipo, ubicacion, estado, cultivoId }) {
    const id = crypto.randomUUID();
    const res = await query(
      `INSERT INTO sensores.sensores (id, tipo, ubicacion, estado, cultivo_id) 
       VALUES ($1, $2, $3, $4, $5) 
       RETURNING id, tipo, ubicacion, estado, cultivo_id`,
      [id, tipo, ubicacion, estado, cultivoId]
    );
    return res.rows[0];
  }

  async update(id, { tipo, ubicacion, estado, cultivoId }) {
    const res = await query(
      `UPDATE sensores.sensores 
       SET tipo = $1, ubicacion = $2, estado = $3, cultivo_id = $4 
       WHERE id = $5 
       RETURNING id, tipo, ubicacion, estado, cultivo_id`,
      [tipo, ubicacion, estado, cultivoId, id]
    );
    return res.rows[0] || null;
  }

  async delete(id) {
    const res = await query(
      'DELETE FROM sensores.sensores WHERE id = $1 RETURNING id',
      [id]
    );
    return res.rowCount > 0;
  }
}

export const sensorRepository = new SensorRepository();
