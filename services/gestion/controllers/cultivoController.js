import { cultivoRepository } from '../repositories/cultivoRepository.js';
import { productorRepository } from '../repositories/productorRepository.js';
import { formatCultivoOutput } from '../dto/cultivoDto.js';
import { NotFoundError, ConflictError } from '../../shared/errors.js';
import { interServiceRequest } from '../../shared/http.js';

export class CultivoController {
  async getAll(req, res, next) {
    try {
      const rows = await cultivoRepository.findAll();
      res.status(200).json(rows.map(formatCultivoOutput));
    } catch (error) {
      next(error);
    }
  }

  async getById(req, res, next) {
    try {
      const cultivo = await cultivoRepository.findById(req.params.id);
      if (!cultivo) {
        throw new NotFoundError(`Cultivo con ID ${req.params.id} no encontrado`);
      }
      res.status(200).json(formatCultivoOutput(cultivo));
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      // Validar que el productor exista dentro de la misma base de datos / esquema
      const productor = await productorRepository.findById(req.body.productorId);
      if (!productor) {
        throw new NotFoundError(`No se puede crear el cultivo: El Productor con ID ${req.body.productorId} no existe`);
      }

      const nuevo = await cultivoRepository.create(req.body);
      res.status(201).json(formatCultivoOutput(nuevo));
    } catch (error) {
      next(error);
    }
  }

  async update(req, res, next) {
    try {
      const existe = await cultivoRepository.findById(req.params.id);
      if (!existe) {
        throw new NotFoundError(`Cultivo con ID ${req.params.id} no encontrado`);
      }

      const productor = await productorRepository.findById(req.body.productorId);
      if (!productor) {
        throw new NotFoundError(`El Productor con ID ${req.body.productorId} no existe`);
      }

      const actualizado = await cultivoRepository.update(req.params.id, req.body);
      res.status(200).json(formatCultivoOutput(actualizado));
    } catch (error) {
      next(error);
    }
  }

  async delete(req, res, next) {
    try {
      const cultivo = await cultivoRepository.findById(req.params.id);
      if (!cultivo) {
        throw new NotFoundError(`Cultivo con ID ${req.params.id} no encontrado`);
      }

      const sensorServiceUrl = process.env.SENSOR_SERVICE_URL || 'http://localhost:3002';
      const alertaServiceUrl = process.env.ALERTA_SERVICE_URL || 'http://localhost:3004';

      // 1. Validar por HTTP que no tenga sensores asociados en SensorService
      const sensores = await interServiceRequest(
        `${sensorServiceUrl}/api/sensores?cultivoId=${req.params.id}`,
        { method: 'GET' },
        'SensorService'
      );
      if (Array.isArray(sensores) && sensores.length > 0) {
        throw new ConflictError(
          `No se puede eliminar el cultivo: tiene ${sensores.length} sensor(es) asociado(s)`
        );
      }

      // 2. Validar por HTTP que no tenga alertas asociadas en AlertaService
      const alertas = await interServiceRequest(
        `${alertaServiceUrl}/api/alertas?cultivoId=${req.params.id}`,
        { method: 'GET' },
        'AlertaService'
      );
      if (Array.isArray(alertas) && alertas.length > 0) {
        throw new ConflictError(
          `No se puede eliminar el cultivo: tiene ${alertas.length} alerta(s) asociada(s)`
        );
      }

      await cultivoRepository.delete(req.params.id);
      res.status(204).send();
    } catch (error) {
      next(error);
    }
  }
}

export const cultivoController = new CultivoController();
