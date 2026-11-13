import { medicionRepository } from '../repositories/medicionRepository.js';
import { formatMedicionOutput } from '../dto/medicionDto.js';
import { evaluarReglasMedicion } from '../rules.js';
import { NotFoundError, ConflictError } from '../../shared/errors.js';
import { interServiceRequest } from '../../shared/http.js';

export class MedicionController {
  async getAll(req, res, next) {
    try {
      const rows = await medicionRepository.findAll(req.query);
      res.status(200).json(rows.map(formatMedicionOutput));
    } catch (error) {
      next(error);
    }
  }

  async getById(req, res, next) {
    try {
      const medicion = await medicionRepository.findById(req.params.id);
      if (!medicion) {
        throw new NotFoundError(`Medición con ID ${req.params.id} no encontrada`);
      }
      res.status(200).json(formatMedicionOutput(medicion));
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      const sensorUrl = process.env.SENSOR_SERVICE_URL || 'http://localhost:3002';
      const gestionUrl = process.env.GESTION_SERVICE_URL || 'http://localhost:3001';
      const alertaUrl = process.env.ALERTA_SERVICE_URL || 'http://localhost:3004';

      // 1. Consultar SensorService por HTTP y comprobar que exista
      const sensor = await interServiceRequest(
        `${sensorUrl}/api/sensores/${req.body.sensorId}`,
        { method: 'GET' },
        'SensorService'
      );

      // 2. Comprobar que el sensor esté activo
      if (sensor.estado !== 'activo') {
        throw new ConflictError(
          `No se puede registrar la medición: el sensor con ID ${req.body.sensorId} no está activo (estado actual: '${sensor.estado}')`
        );
      }

      // 3. Comprobar que el cultivo asociado al sensor exista en GestionService
      await interServiceRequest(
        `${gestionUrl}/api/cultivos/${sensor.cultivoId}`,
        { method: 'GET' },
        'GestionService'
      );

      // 4. Guardar la medición en PostgreSQL (registro histórico)
      const nuevaMedicion = await medicionRepository.create(req.body);

      // 5. Evaluar reglas automáticas de agricultura inteligente
      const alertasAEmitir = evaluarReglasMedicion(
        { temperatura: nuevaMedicion.temperatura, humedad: nuevaMedicion.humedad },
        sensor.cultivoId,
        req.body.fecha
      );

      if (alertasAEmitir.length === 0) {
        await medicionRepository.saveProcessingStatus(nuevaMedicion.id, {
          estado: 'sin_alertas',
          alertasGeneradas: [],
        });

        return res.status(201).json({
          medicion: formatMedicionOutput(nuevaMedicion),
          alertas: [],
          procesamiento: {
            estado: 'sin_alertas',
            totalAlertas: 0,
            alertasEnviadas: 0,
            mensaje: 'Medición registrada. Los valores evaluados se encuentran dentro de los parámetros normales.',
          },
        });
      }

      // 6. Enviar alertas a AlertaService mediante HTTP
      const alertasCreadas = [];
      let falloEnvio = false;
      let mensajeError = null;

      for (const alertaPayload of alertasAEmitir) {
        try {
          const alertaCreada = await interServiceRequest(
            `${alertaUrl}/api/alertas`,
            {
              method: 'POST',
              body: alertaPayload,
            },
            'AlertaService'
          );
          alertasCreadas.push(alertaCreada);
        } catch (err) {
          falloEnvio = true;
          mensajeError = err.message;
          console.error(`[MedicionService] Error al enviar alerta a AlertaService: ${err.message}`);
          break;
        }
      }

      // 7. Manejo de fallos parciales: si falló la creación de alertas, NO se elimina la medición
      const estadoProcesamiento = falloEnvio ? 'pendiente' : 'completado';
      await medicionRepository.saveProcessingStatus(nuevaMedicion.id, {
        estado: estadoProcesamiento,
        alertasGeneradas: alertasCreadas,
        error: mensajeError,
      });

      res.status(201).json({
        medicion: formatMedicionOutput(nuevaMedicion),
        alertas: alertasCreadas,
        procesamiento: {
          estado: estadoProcesamiento,
          totalAlertas: alertasAEmitir.length,
          alertasEnviadas: alertasCreadas.length,
          mensaje: falloEnvio
            ? 'Medición persistida con éxito, pero la notificación de alertas quedó pendiente. Utilice POST /api/mediciones/' + nuevaMedicion.id + '/procesar para reintentar.'
            : 'Medición registrada y alertas automáticas procesadas exitosamente.',
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * Reintenta el procesamiento de alertas para una medición que quedó pendiente
   */
  async procesarReintento(req, res, next) {
    try {
      const { id } = req.params;
      const medicion = await medicionRepository.findById(id);
      if (!medicion) {
        throw new NotFoundError(`Medición con ID ${id} no encontrada`);
      }

      const sensorUrl = process.env.SENSOR_SERVICE_URL || 'http://localhost:3002';
      const alertaUrl = process.env.ALERTA_SERVICE_URL || 'http://localhost:3004';

      const sensor = await interServiceRequest(
        `${sensorUrl}/api/sensores/${medicion.sensor_id}`,
        { method: 'GET' },
        'SensorService'
      );

      const alertasAEmitir = evaluarReglasMedicion(
        { temperatura: medicion.temperatura, humedad: medicion.humedad },
        sensor.cultivoId,
        medicion.fecha
      );

      const alertasCreadas = [];
      let falloEnvio = false;
      let mensajeError = null;

      for (const alertaPayload of alertasAEmitir) {
        try {
          const alerta = await interServiceRequest(
            `${alertaUrl}/api/alertas`,
            {
              method: 'POST',
              body: alertaPayload,
            },
            'AlertaService'
          );
          alertasCreadas.push(alerta);
        } catch (err) {
          falloEnvio = true;
          mensajeError = err.message;
          break;
        }
      }

      const nuevoEstado = falloEnvio ? 'pendiente' : (alertasAEmitir.length === 0 ? 'sin_alertas' : 'completado');
      await medicionRepository.saveProcessingStatus(medicion.id, {
        estado: nuevoEstado,
        alertasGeneradas: alertasCreadas,
        error: mensajeError,
      });

      res.status(200).json({
        medicion: formatMedicionOutput(medicion),
        alertas: alertasCreadas,
        procesamiento: {
          estado: nuevoEstado,
          totalAlertas: alertasAEmitir.length,
          alertasEnviadas: alertasCreadas.length,
          mensaje: falloEnvio
            ? 'El reintento no pudo completar el envío de todas las alertas. Permanece en estado pendiente.'
            : 'Reintento completado con éxito. Todas las alertas fueron sincronizadas.',
        },
      });
    } catch (error) {
      next(error);
    }
  }
}

export const medicionController = new MedicionController();
