import { productorRepository } from '../repositories/productorRepository.js';
import { formatProductorOutput } from '../dto/productorDto.js';
import { NotFoundError, ConflictError } from '../../shared/errors.js';

export class ProductorController {
  async getAll(req, res, next) {
    try {
      const rows = await productorRepository.findAll();
      const result = rows.map(formatProductorOutput);
      res.status(200).json(result);
    } catch (error) {
      next(error);
    }
  }

  async getById(req, res, next) {
    try {
      const productor = await productorRepository.findById(req.params.id);
      if (!productor) {
        throw new NotFoundError(`Productor con ID ${req.params.id} no encontrado`);
      }
      res.status(200).json(formatProductorOutput(productor));
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      const nuevo = await productorRepository.create(req.body);
      res.status(201).json(formatProductorOutput(nuevo));
    } catch (error) {
      next(error);
    }
  }

  async update(req, res, next) {
    try {
      const existe = await productorRepository.findById(req.params.id);
      if (!existe) {
        throw new NotFoundError(`Productor con ID ${req.params.id} no encontrado`);
      }
      const actualizado = await productorRepository.update(req.params.id, req.body);
      res.status(200).json(formatProductorOutput(actualizado));
    } catch (error) {
      next(error);
    }
  }

  async delete(req, res, next) {
    try {
      const existe = await productorRepository.findById(req.params.id);
      if (!existe) {
        throw new NotFoundError(`Productor con ID ${req.params.id} no encontrado`);
      }

      // Validación de regla de negocio: no eliminar si tiene cultivos asociados
      const cantidadCultivos = await productorRepository.countCultivosByProductorId(req.params.id);
      if (cantidadCultivos > 0) {
        throw new ConflictError(
          `No se puede eliminar el productor: tiene ${cantidadCultivos} cultivo(s) asociado(s)`
        );
      }

      await productorRepository.delete(req.params.id);
      res.status(204).send();
    } catch (error) {
      next(error);
    }
  }
}

export const productorController = new ProductorController();
