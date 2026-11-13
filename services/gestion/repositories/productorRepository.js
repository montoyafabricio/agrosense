import crypto from 'node:crypto';
import { query } from '../../../database/connection.js';

export class ProductorRepository {
  async findAll() {
    const res = await query(
      'SELECT id, nombre, contacto FROM gestion.productores ORDER BY created_at ASC'
    );
    return res.rows;
  }

  async findById(id) {
    const res = await query(
      'SELECT id, nombre, contacto FROM gestion.productores WHERE id = $1',
      [id]
    );
    return res.rows[0] || null;
  }

  async create({ nombre, contacto }) {
    const id = crypto.randomUUID();
    const res = await query(
      'INSERT INTO gestion.productores (id, nombre, contacto) VALUES ($1, $2, $3) RETURNING id, nombre, contacto',
      [id, nombre, contacto]
    );
    return res.rows[0];
  }

  async update(id, { nombre, contacto }) {
    const res = await query(
      'UPDATE gestion.productores SET nombre = $1, contacto = $2 WHERE id = $3 RETURNING id, nombre, contacto',
      [nombre, contacto, id]
    );
    return res.rows[0] || null;
  }

  async delete(id) {
    const res = await query(
      'DELETE FROM gestion.productores WHERE id = $1 RETURNING id',
      [id]
    );
    return res.rowCount > 0;
  }

  async countCultivosByProductorId(productorId) {
    const res = await query(
      'SELECT COUNT(*)::int AS count FROM gestion.cultivos WHERE productor_id = $1',
      [productorId]
    );
    return res.rows[0].count;
  }
}

export const productorRepository = new ProductorRepository();
