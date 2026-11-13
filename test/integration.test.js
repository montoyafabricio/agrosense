import test from 'node:test';
import assert from 'node:assert/strict';
import { testConnection } from '../database/connection.js';

// NOTA: Las pruebas de integración verifican el flujo completo de extremo a extremo
// a través de llamadas HTTP contra los endpoints del API Gateway o servicios locales.
// Utilizan la base de datos de pruebas configurada (DB_NAME_TEST) para no afectar desarrollo.

test('Pruebas de Integración - Verificación de entorno y base de datos de pruebas', async (t) => {
  const dbOk = await testConnection();
  if (!dbOk) {
    t.skip('Omitiendo pruebas de integración con PostgreSQL: el servidor PostgreSQL no está disponible en este entorno.');
    return;
  }
  assert.ok(true, 'Conexión a PostgreSQL confirmada');
});

test('Pruebas de Integración - Flujo Completo End-to-End', async (t) => {
  const dbOk = await testConnection();
  if (!dbOk) {
    t.skip('PostgreSQL no disponible. Inicie PostgreSQL para ejecutar el flujo completo.');
    return;
  }

  // Las pruebas se ejecutan directamente contra el Gateway en el puerto 8080 si está activo,
  // o se comprueba la disponibilidad del Gateway.
  const gatewayUrl = 'http://localhost:8080';
  let isGatewayUp = false;
  try {
    const health = await fetch(`${gatewayUrl}/health`, { signal: AbortSignal.timeout(1000) });
    isGatewayUp = health.ok;
  } catch {
    isGatewayUp = false;
  }

  if (!isGatewayUp) {
    t.skip('El API Gateway (puerto 8080) no está activo. Inicie el sistema con "npm start" para ejecutar pruebas contra endpoints en vivo.');
    return;
  }

  // 1. Crear Productor
  const resProductor = await fetch(`${gatewayUrl}/api/productores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre: 'Productor Test', contacto: 'test@agrosense.com' }),
  });
  assert.equal(resProductor.status, 201, 'Debe crear productor');
  const productor = await resProductor.json();
  assert.ok(productor.id);

  // 2. Crear Cultivo
  const resCultivo = await fetch(`${gatewayUrl}/api/cultivos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      especie: 'Maíz',
      superficie: 5.0,
      fechaSiembra: '2026-10-15',
      productorId: productor.id,
    }),
  });
  assert.equal(resCultivo.status, 201, 'Debe crear cultivo');
  const cultivo = await resCultivo.json();
  assert.ok(cultivo.id);

  // 3. Crear Sensor activo
  const resSensor = await fetch(`${gatewayUrl}/api/sensores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tipo: 'mixto',
      ubicacion: 'Lote Norte',
      estado: 'activo',
      cultivoId: cultivo.id,
    }),
  });
  assert.equal(resSensor.status, 201, 'Debe crear sensor activo');
  const sensor = await resSensor.json();
  assert.ok(sensor.id);

  // 4. Registrar Medición con condiciones de alerta (Temp: 38°C > 35 -> CALOR, Humedad: 20% < 30 -> RIEGO)
  const resMedicion = await fetch(`${gatewayUrl}/api/mediciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      temperatura: 38.0,
      humedad: 20.0,
      fecha: new Date().toISOString(),
      sensorId: sensor.id,
    }),
  });
  assert.equal(resMedicion.status, 201, 'Debe registrar medición');
  const medicionBody = await resMedicion.json();
  assert.ok(medicionBody.medicion.id);
  assert.equal(medicionBody.alertas.length, 2, 'Debió generar alertas de RIEGO y CALOR');

  // 5. Consultar Alertas por cultivoId
  const resAlertas = await fetch(`${gatewayUrl}/api/alertas?cultivoId=${cultivo.id}`);
  assert.equal(resAlertas.status, 200);
  const alertas = await resAlertas.json();
  assert.ok(alertas.length >= 2, 'Deben existir al menos las 2 alertas generadas');

  // 6. Probar que un sensor inactivo rechaza mediciones (409)
  const resSensorInactivo = await fetch(`${gatewayUrl}/api/sensores`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tipo: 'temperatura',
      ubicacion: 'Lote Sur',
      estado: 'inactivo',
      cultivoId: cultivo.id,
    }),
  });
  const sensorInactivo = await resSensorInactivo.json();

  const resMedicionInactiva = await fetch(`${gatewayUrl}/api/mediciones`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      temperatura: 25.0,
      humedad: 50.0,
      fecha: new Date().toISOString(),
      sensorId: sensorInactivo.id,
    }),
  });
  assert.equal(resMedicionInactiva.status, 409, 'Sensor inactivo debe responder 409 Conflict');

  // 7. Restricciones de eliminación: Intentar borrar cultivo con sensores debe fallar (409)
  const resDelCultivo = await fetch(`${gatewayUrl}/api/cultivos/${cultivo.id}`, { method: 'DELETE' });
  assert.equal(resDelCultivo.status, 409, 'No debe permitir eliminar cultivo con sensores asociados');

  // 8. Reintentar procesamiento y verificar no duplicación
  const resReintento = await fetch(`${gatewayUrl}/api/mediciones/${medicionBody.medicion.id}/procesar`, {
    method: 'POST',
  });
  assert.equal(resReintento.status, 200, 'Reintento debe responder 200');
  
  // Comprobar que no se hayan duplicado las alertas en la base de datos
  const resAlertasDespues = await fetch(`${gatewayUrl}/api/alertas?cultivoId=${cultivo.id}`);
  const alertasDespues = await resAlertasDespues.json();
  assert.equal(alertasDespues.length, alertas.length, 'No deben duplicarse alertas en reintentos');
});
