import { Router } from 'express';
import { alertaController } from './controllers/alertaController.js';
import { validateBody, validateParams, validateQuery } from '../shared/validation.js';
import {
  CreateAlertaDTO,
  AlertaIdParamDTO,
  AlertaQueryDTO,
} from './dto/alertaDto.js';

export const router = Router();

router.get(
  '/api/alertas',
  validateQuery(AlertaQueryDTO),
  (req, res, next) => alertaController.getAll(req, res, next)
);

router.post(
  '/api/alertas',
  validateBody(CreateAlertaDTO),
  (req, res, next) => alertaController.create(req, res, next)
);

router.get(
  '/api/alertas/:id',
  validateParams(AlertaIdParamDTO),
  (req, res, next) => alertaController.getById(req, res, next)
);
