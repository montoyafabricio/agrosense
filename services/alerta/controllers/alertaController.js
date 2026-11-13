import { alertaRepository } from '../repositories/alertaRepository.js';
import { formatAlertaOutput } from '../dto/alertaDto.js';
import { NotFoundError } from '../../shared/errors.js';
import { interServiceRequest } from '../../shared/http.js';

export class AlertaController {
  async getAll(req, res, next) {
    try {
      const rows = await alertaRepository.findAll(req.query);
      res.status(200).json(rows.map(formatAlertaOutput));
    } catch (error) {
      next(error);
    }
  }

  async getById(req, res, next) {
    try {
      const alerta = await alertaRepository.findById(req.params.id);
      if (!alerta) {
        throw new NotFoundError(`Alerta con ID ${req.params.id} no encontrada`);
      }
      res.status(200).json(formatAlertaOutput(alerta));
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      const gestionUrl = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';

      // 1. Validar por HTTP que el cultivo exista en GestionService
      await interServiceRequest(
        `${gestionUrl}/api/cultivos/${req.body.cultivoId}`,
        { method: 'GET' },
        'GestionService'
      );

      // 2. Control de Idempotencia: Evitar duplicar alertas del mismo tipo y cultivo en reintentos
      const duplicada = await alertaRepository.findDuplicate({
        cultivoId: req.body.cultivoId,
        tipo: req.body.tipo,
        fecha: req.body.fecha,
      });

      if (duplicada) {
        // Devuelve la alerta existente para garantizar idempotencia en reintentos
        return res.status(200).json(formatAlertaOutput(duplicada));
      }

      const nueva = await alertaRepository.create(req.body);
      res.status(201).json(formatAlertaOutput(nueva));
    } catch (error) {
      next(error);
    }
  }
}

export const alertaController = new AlertaController();
