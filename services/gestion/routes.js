import { Router } from 'express';
import { productorController } from './controllers/productorController.js';
import { cultivoController } from './controllers/cultivoController.js';
import { validateBody, validateParams } from '../shared/validation.js';
import {
  CreateProductorDTO,
  UpdateProductorDTO,
  ProductorIdParamDTO,
} from './dto/productorDto.js';
import {
  CreateCultivoDTO,
  UpdateCultivoDTO,
  CultivoIdParamDTO,
} from './dto/cultivoDto.js';

export const router = Router();

// ================= Rutas Productores =================
router.get('/api/productores', (req, res, next) => productorController.getAll(req, res, next));
router.post(
  '/api/productores',
  validateBody(CreateProductorDTO),
  (req, res, next) => productorController.create(req, res, next)
);
router.get(
  '/api/productores/:id',
  validateParams(ProductorIdParamDTO),
  (req, res, next) => productorController.getById(req, res, next)
);
router.put(
  '/api/productores/:id',
  validateParams(ProductorIdParamDTO),
  validateBody(UpdateProductorDTO),
  (req, res, next) => productorController.update(req, res, next)
);
router.delete(
  '/api/productores/:id',
  validateParams(ProductorIdParamDTO),
  (req, res, next) => productorController.delete(req, res, next)
);

// ================= Rutas Cultivos =================
router.get('/api/cultivos', (req, res, next) => cultivoController.getAll(req, res, next));
router.post(
  '/api/cultivos',
  validateBody(CreateCultivoDTO),
  (req, res, next) => cultivoController.create(req, res, next)
);
router.get(
  '/api/cultivos/:id',
  validateParams(CultivoIdParamDTO),
  (req, res, next) => cultivoController.getById(req, res, next)
);
router.put(
  '/api/cultivos/:id',
  validateParams(CultivoIdParamDTO),
  validateBody(UpdateCultivoDTO),
  (req, res, next) => cultivoController.update(req, res, next)
);
router.delete(
  '/api/cultivos/:id',
  validateParams(CultivoIdParamDTO),
  (req, res, next) => cultivoController.delete(req, res, next)
);
