/**
 * ============================================================================
 * AGROSENSE - REGLAS AUTOMÁTICAS DE AGRICULTURA INTELIGENTE
 * ============================================================================
 * 
 * NOTA Y ADVERTENCIA DE DOMINIO:
 * Los umbrales definidos en este módulo son estrictamente demostrativos e ilustrativos
 * para fines académicos. No representan valores agronómicos universales adecuados
 * para todas las especies, etapas de crecimiento o tipos de suelo.
 * El sistema emite recomendaciones informativas y NO controla directamente actuadores,
 * bombas de agua, electroválvulas ni sistemas físicos de riego.
 */

export const REGLA_HUMEDAD_BAJA_UMBRAL = 30.0;
export const REGLA_HUMEDAD_ALTA_UMBRAL = 80.0;
export const REGLA_TEMPERATURA_ALTA_UMBRAL = 35.0;

/**
 * Evalúa las lecturas de una medición agronómica y genera las recomendaciones/alertas correspondientes.
 * 
 * Reglas evaluadas (con comparación estricta):
 * 1. Humedad < 30% -> RIEGO (prioridad alta)
 * 2. Humedad > 80% -> EXCESO_HUMEDAD (prioridad media)
 * 3. Temperatura > 35°C -> CALOR (prioridad alta)
 * 
 * @param {object} medicion - Objeto con temperatura y humedad
 * @param {number} medicion.temperatura - Grados Celsius
 * @param {number} medicion.humedad - Porcentaje (0 - 100)
 * @param {string} cultivoId - UUID del cultivo al que pertenece el sensor
 * @param {string} [fechaReferencia] - Fecha ISO opcional para pruebas reproducibles
 * @returns {Array<{tipo: string, descripcion: string, prioridad: 'baja'|'media'|'alta', fecha: string, cultivoId: string}>}
 */
export function evaluarReglasMedicion({ temperatura, humedad }, cultivoId, fechaReferencia = null) {
  const alertas = [];
  const fechaActual = fechaReferencia || new Date().toISOString();

  // Regla 1: Humedad baja (estricta: humedad < 30)
  if (humedad < REGLA_HUMEDAD_BAJA_UMBRAL) {
    alertas.push({
      tipo: 'RIEGO',
      descripcion: 'Revisar la necesidad de riego.',
      prioridad: 'alta',
      fecha: fechaActual,
      cultivoId,
    });
  }

  // Regla 2: Humedad elevada (estricta: humedad > 80)
  if (humedad > REGLA_HUMEDAD_ALTA_UMBRAL) {
    alertas.push({
      tipo: 'EXCESO_HUMEDAD',
      descripcion: 'Revisar el drenaje antes de regar.',
      prioridad: 'media',
      fecha: fechaActual,
      cultivoId,
    });
  }

  // Regla 3: Temperatura elevada (estricta: temperatura > 35)
  if (temperatura > REGLA_TEMPERATURA_ALTA_UMBRAL) {
    alertas.push({
      tipo: 'CALOR',
      descripcion: 'Revisar las condiciones del cultivo por temperatura elevada.',
      prioridad: 'alta',
      fecha: fechaActual,
      cultivoId,
    });
  }

  return alertas;
}
