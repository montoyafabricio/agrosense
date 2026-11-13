import { sensorRepository } from '../repositories/sensorRepository.js';
import { formatSensorOutput } from '../dto/sensorDto.js';
import { NotFoundError, ConflictError } from '../../shared/errors.js';
import { interServiceRequest } from '../../shared/http.js';

export class SensorController {
  async getAll(req, res, next) {
    try {
      const rows = await sensorRepository.findAll(req.query);
      res.status(200).json(rows.map(formatSensorOutput));
    } catch (error) {
      next(error);
    }
  }

  async getById(req, res, next) {
    try {
      const sensor = await sensorRepository.findById(req.params.id);
      if (!sensor) {
        throw new NotFoundError(`Sensor con ID ${req.params.id} no encontrado`);
      }
      res.status(200).json(formatSensorOutput(sensor));
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      const gestionUrl = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';

      // Validar por HTTP que el cultivo asociado exista en GestionService
      await interServiceRequest(
        `${gestionUrl}/api/cultivos/${req.body.cultivoId}`,
        { method: 'GET' },
        'GestionService'
      );

      const nuevo = await sensorRepository.create(req.body);
      res.status(201).json(formatSensorOutput(nuevo));
    } catch (error) {
      next(error);
    }
  }

  async update(req, res, next) {
    try {
      const existe = await sensorRepository.findById(req.params.id);
      if (!existe) {
        throw new NotFoundError(`Sensor con ID ${req.params.id} no encontrado`);
      }

      const gestionUrl = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';
      // Validar por HTTP que el cultivo exista
      await interServiceRequest(
        `${gestionUrl}/api/cultivos/${req.body.cultivoId}`,
        { method: 'GET' },
        'GestionService'
      );

      const actualizado = await sensorRepository.update(req.params.id, req.body);
      res.status(200).json(formatSensorOutput(actualizado));
    } catch (error) {
      next(error);
    }
  }

  async delete(req, res, next) {
    try {
      const existe = await sensorRepository.findById(req.params.id);
      if (!existe) {
        throw new NotFoundError(`Sensor con ID ${req.params.id} no encontrado`);
      }

      const medicionUrl = process.env.MEDICION_SERVICE_URL || 'http://localhost:3003';

      // Validar por HTTP que no tenga mediciones asociadas en MedicionService
      const mediciones = await interServiceRequest(
        `${medicionUrl}/api/mediciones?sensorId=${req.params.id}`,
        { method: 'GET' },
        'MedicionService'
      );

      if (Array.isArray(mediciones) && mediciones.length > 0) {
        throw new ConflictError(
          `No se puede eliminar el sensor: tiene ${mediciones.length} medición(es) histórica(s) registrada(s)`
        );
      }

      await sensorRepository.delete(req.params.id);
      res.status(204).send();
    } catch (error) {
      next(error);
    }
  }
}

export const sensorController = new SensorController();
