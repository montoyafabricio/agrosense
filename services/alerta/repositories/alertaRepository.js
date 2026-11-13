import crypto from 'node:crypto';
import { query } from '../../../database/connection.js';

export class AlertaRepository {
  async findAll(filter = {}) {
    let sql = 'SELECT id, tipo, descripcion, prioridad, fecha, cultivo_id FROM alertas.alertas';
    const params = [];
    const conditions = [];

    if (filter.cultivoId) {
      params.push(filter.cultivoId);
      conditions.push(`cultivo_id = $${params.length}`);
    }

    if (filter.prioridad) {
      params.push(filter.prioridad);
      conditions.push(`prioridad = $${params.length}`);
    }

    if (conditions.length > 0) {
      sql += ' WHERE ' + conditions.join(' AND ');
    }

    sql += ' ORDER BY fecha DESC';

    const res = await query(sql, params);
    return res.rows;
  }

  async findById(id) {
    const res = await query(
      'SELECT id, tipo, descripcion, prioridad, fecha, cultivo_id FROM alertas.alertas WHERE id = $1',
      [id]
    );
    return res.rows[0] || null;
  }

  /**
   * Busca si ya existe una alerta idéntica para evitar duplicados en reintentos
   */
  async findDuplicate({ cultivoId, tipo, fecha }) {
    // Si la fecha coincide dentro de una ventana de 2 minutos para el mismo cultivo y tipo
    const res = await query(
      `SELECT id, tipo, descripcion, prioridad, fecha, cultivo_id 
       FROM alertas.alertas 
       WHERE cultivo_id = $1 
         AND tipo = $2 
         AND ABS(EXTRACT(EPOCH FROM (fecha - $3::timestamptz))) <= 120
       LIMIT 1`,
      [cultivoId, tipo, fecha]
    );
    return res.rows[0] || null;
  }

  async create({ tipo, descripcion, prioridad, fecha, cultivoId }) {
    const id = crypto.randomUUID();
    const res = await query(
      `INSERT INTO alertas.alertas (id, tipo, descripcion, prioridad, fecha, cultivo_id) 
       VALUES ($1, $2, $3, $4, $5, $6) 
       RETURNING id, tipo, descripcion, prioridad, fecha, cultivo_id`,
      [id, tipo, descripcion, prioridad, fecha, cultivoId]
    );
    return res.rows[0];
  }
}

export const alertaRepository = new AlertaRepository();
