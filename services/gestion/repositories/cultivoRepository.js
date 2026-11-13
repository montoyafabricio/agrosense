import crypto from 'node:crypto';
import { query } from '../../../database/connection.js';

export class CultivoRepository {
  async findAll() {
    const res = await query(
      `SELECT id, especie, superficie, fecha_siembra, productor_id 
       FROM gestion.cultivos 
       ORDER BY created_at ASC`
    );
    return res.rows;
  }

  async findById(id) {
    const res = await query(
      `SELECT id, especie, superficie, fecha_siembra, productor_id 
       FROM gestion.cultivos 
       WHERE id = $1`,
      [id]
    );
    return res.rows[0] || null;
  }

  async create({ especie, superficie, fechaSiembra, productorId }) {
    const id = crypto.randomUUID();
    const res = await query(
      `INSERT INTO gestion.cultivos (id, especie, superficie, fecha_siembra, productor_id) 
       VALUES ($1, $2, $3, $4, $5) 
       RETURNING id, especie, superficie, fecha_siembra, productor_id`,
      [id, especie, superficie, fechaSiembra, productorId]
    );
    return res.rows[0];
  }

  async update(id, { especie, superficie, fechaSiembra, productorId }) {
    const res = await query(
      `UPDATE gestion.cultivos 
       SET especie = $1, superficie = $2, fecha_siembra = $3, productor_id = $4 
       WHERE id = $5 
       RETURNING id, especie, superficie, fecha_siembra, productor_id`,
      [especie, superficie, fechaSiembra, productorId, id]
    );
    return res.rows[0] || null;
  }

  async delete(id) {
    const res = await query(
      'DELETE FROM gestion.cultivos WHERE id = $1 RETURNING id',
      [id]
    );
    return res.rowCount > 0;
  }
}

export const cultivoRepository = new CultivoRepository();
