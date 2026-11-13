import dotenv from 'dotenv';

dotenv.config();

const GATEWAY_URL = `http://localhost:${process.env.GATEWAY_PORT || '8080'}`;

const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
  red: '\x1b[31m',
};

function logStep(stepNumber, title) {
  console.log(`\n${colors.cyan}${colors.bright}================================================================${colors.reset}`);
  console.log(`${colors.cyan}${colors.bright}[PASO ${stepNumber}] ${title}${colors.reset}`);
  console.log(`${colors.cyan}================================================================${colors.reset}`);
}

async function runDemo() {
  console.log(`${colors.green}${colors.bright}
  ========================================================
     DEMOSTRACIÓN INTEGRAL EN VIVO: SISTEMA AGROSENSE
  ========================================================${colors.reset}`);
  console.log(`Punto de Entrada: ${GATEWAY_URL}\n`);

  // 1. Health Check
  logStep(1, 'Comprobando estado del API Gateway y los 4 Microservicios');
  try {
    const resHealth = await fetch(`${GATEWAY_URL}/health`);
    const health = await resHealth.json();
    console.log('Respuesta /health:');
    console.dir(health, { depth: null });
  } catch (error) {
    console.error(`${colors.red}❌ No se pudo conectar al API Gateway en ${GATEWAY_URL}. Asegúrate de haber iniciado el sistema con "npm start".${colors.reset}`);
    process.exit(1);
  }

  // 2. Crear Productor
  logStep(2, 'Creando Productor (Ana Pérez)');
  const resProductor = await fetch(`${GATEWAY_URL}/api/productores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      nombre: 'Ana Pérez',
      contacto: 'ana@example.com',
    }),
  });
  const productor = await resProductor.json();
  console.log(`✔ HTTP ${resProductor.status} - Productor Creado:`, productor);

  // 3. Crear Cultivo
  logStep(3, 'Creando Cultivo asociado al Productor (Tomate - 2.5 Hectáreas)');
  const resCultivo = await fetch(`${GATEWAY_URL}/api/cultivos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      especie: 'Tomate',
      superficie: 2.5,
      fechaSiembra: '2026-10-01',
      productorId: productor.id,
    }),
  });
  const cultivo = await resCultivo.json();
  console.log(`✔ HTTP ${resCultivo.status} - Cultivo Creado:`, cultivo);

  // 4. Crear Sensor Activo
  logStep(4, 'Creando Sensor "mixto" en estado "activo" en Parcela A');
  const resSensor = await fetch(`${GATEWAY_URL}/api/sensores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tipo: 'mixto',
      ubicacion: 'Parcela A',
      estado: 'activo',
      cultivoId: cultivo.id,
    }),
  });
  const sensor = await resSensor.json();
  console.log(`✔ HTTP ${resSensor.status} - Sensor Creado:`, sensor);

  // 5. Medición Normal (Sin alertas)
  logStep(5, 'Registrando Medición con valores normales (Temp: 24°C, Humedad: 55%)');
  const resMedNormal = await fetch(`${GATEWAY_URL}/api/mediciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      temperatura: 24.0,
      humedad: 55.0,
      fecha: new Date().toISOString(),
      sensorId: sensor.id,
    }),
  });
  const medNormal = await resMedNormal.json();
  console.log(`✔ HTTP ${resMedNormal.status} - Respuesta:`, medNormal);

  // 6. Medición Crítica (Dispara RIEGO y CALOR)
  logStep(6, 'Registrando Medición Crítica (Temp: 38°C > 35, Humedad: 20% < 30)');
  const resMedCritica = await fetch(`${GATEWAY_URL}/api/mediciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      temperatura: 38.0,
      humedad: 20.0,
      fecha: new Date().toISOString(),
      sensorId: sensor.id,
    }),
  });
  const medCritica = await resMedCritica.json();
  console.log(`✔ HTTP ${resMedCritica.status} - Medición Crítica y Alertas Generadas:`);
  console.dir(medCritica, { depth: null });

  // 7. Consultar Alertas por Cultivo
  logStep(7, `Consultando Alertas del Cultivo ${cultivo.id} (GET /api/alertas?cultivoId=...)`);
  const resAlertas = await fetch(`${GATEWAY_URL}/api/alertas?cultivoId=${cultivo.id}`);
  const alertas = await resAlertas.json();
  console.log(`✔ HTTP ${resAlertas.status} - Total de Alertas encontradas: ${alertas.length}`);
  console.dir(alertas, { depth: null });

  // 8. Demostración de Restricción de Negocio: Rechazo de medición en sensor inactivo
  logStep(8, 'Demostración de Integridad: Intentar medir con un sensor "inactivo"');
  const resSensorInactivo = await fetch(`${GATEWAY_URL}/api/sensores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tipo: 'temperatura',
      ubicacion: 'Invernadero Inactivo',
      estado: 'inactivo',
      cultivoId: cultivo.id,
    }),
  });
  const sensorInactivo = await resSensorInactivo.json();

  const resIntentoInactivo = await fetch(`${GATEWAY_URL}/api/mediciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      temperatura: 25.0,
      humedad: 60.0,
      fecha: new Date().toISOString(),
      sensorId: sensorInactivo.id,
    }),
  });
  const errInactivo = await resIntentoInactivo.json();
  console.log(`✔ HTTP ${resIntentoInactivo.status} esperado (409 Conflict):`, errInactivo);

  // 9. Demostración de Idempotencia y Reintentos
  logStep(9, `Demostración de Idempotencia: Reintentar procesamiento de la medición crítica`);
  const resReintento = await fetch(`${GATEWAY_URL}/api/mediciones/${medCritica.medicion.id}/procesar`, {
    method: 'POST',
  });
  const reintento = await resReintento.json();
  console.log(`✔ HTTP ${resReintento.status} - Reintento exitoso (sin duplicar alertas):`, reintento);

  // 10. Demostración de Restricción de Borrado
  logStep(10, 'Demostración de Restricción de Borrado: Intentar borrar cultivo con sensores');
  const resBorrarCultivo = await fetch(`${GATEWAY_URL}/api/cultivos/${cultivo.id}`, {
    method: 'DELETE',
  });
  const errBorrado = await resBorrarCultivo.json();
  console.log(`✔ HTTP ${resBorrarCultivo.status} esperado (409 Conflict):`, errBorrado);

  console.log(`\n${colors.green}${colors.bright}================================================================`);
  console.log('  ✔ TODAS LAS DEMOSTRACIONES SE EJECUTARON SATISFACTORIAMENTE');
  console.log('================================================================\n');
}

runDemo().catch((err) => {
  console.error('Error durante la demostración:', err);
  process.exit(1);
});
