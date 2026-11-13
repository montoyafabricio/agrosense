import { Router } from 'express';
import { medicionController } from './controllers/medicionController.js';
import { validateBody, validateParams, validateQuery } from '../shared/validation.js';
import {
  CreateMedicionDTO,
  MedicionIdParamDTO,
  MedicionQueryDTO,
} from './dto/medicionDto.js';

export const router = Router();

router.get(
  '/api/mediciones',
  validateQuery(MedicionQueryDTO),
  (req, res, next) => medicionController.getAll(req, res, next)
);

router.post(
  '/api/mediciones',
  validateBody(CreateMedicionDTO),
  (req, res, next) => medicionController.create(req, res, next)
);

router.get(
  '/api/mediciones/:id',
  validateParams(MedicionIdParamDTO),
  (req, res, next) => medicionController.getById(req, res, next)
);

router.post(
  '/api/mediciones/:id/procesar',
  validateParams(MedicionIdParamDTO),
  (req, res, next) => medicionController.procesarReintento(req, res, next)
);
