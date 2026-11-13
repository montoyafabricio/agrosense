import { Router } from 'express';
import { sensorController } from './controllers/sensorController.js';
import { validateBody, validateParams, validateQuery } from '../shared/validation.js';
import {
  CreateSensorDTO,
  UpdateSensorDTO,
  SensorIdParamDTO,
  SensorQueryDTO,
} from './dto/sensorDto.js';

export const router = Router();

router.get(
  '/api/sensores',
  validateQuery(SensorQueryDTO),
  (req, res, next) => sensorController.getAll(req, res, next)
);

router.post(
  '/api/sensores',
  validateBody(CreateSensorDTO),
  (req, res, next) => sensorController.create(req, res, next)
);

router.get(
  '/api/sensores/:id',
  validateParams(SensorIdParamDTO),
  (req, res, next) => sensorController.getById(req, res, next)
);

router.put(
  '/api/sensores/:id',
  validateParams(SensorIdParamDTO),
  validateBody(UpdateSensorDTO),
  (req, res, next) => sensorController.update(req, res, next)
);

router.delete(
  '/api/sensores/:id',
  validateParams(SensorIdParamDTO),
  (req, res, next) => sensorController.delete(req, res, next)
);
