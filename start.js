import dotenv from 'dotenv';
import { testConnection, closePool } from './database/connection.js';
import { initializeDatabase } from './database/init.js';
import { startServer as startGestion } from './services/gestion/index.js';
import { startServer as startSensor } from './services/sensor/index.js';
import { startServer as startMedicion } from './services/medicion/index.js';
import { startServer as startAlerta } from './services/alerta/index.js';
import { startServer as startGateway } from './gateway/index.js';

dotenv.config();

const runningServers = [];

async function shutdown(signal) {
  console.log(`\n[AgroSense System] Señal ${signal} recibida. Deteniendo servicios de forma ordenada...`);
  
  for (const { name, server } of runningServers) {
    try {
      await new Promise((resolve) => server.close(resolve));
      console.log(`  ✔ ${name} detenido.`);
    } catch (err) {
      console.error(`  ✖ Error al detener ${name}: ${err.message}`);
    }
  }

  try {
    await closePool();
    console.log('  ✔ Pool de conexiones PostgreSQL cerrado.');
  } catch (err) {
    console.error(`  ✖ Error cerrando pool de base de datos: ${err.message}`);
  }

  console.log('[AgroSense System] Todos los servicios han finalizado correctamente.');
  process.exit(0);
}

process.on('SIGINT', () => shutdown('SIGINT (Ctrl+C)'));
process.on('SIGTERM', () => shutdown('SIGTERM'));

async function main() {
  console.log('================================================================');
  console.log('       AGROSENSE — SISTEMA DE AGRICULTURA INTELIGENTE           ');
  console.log('             Arquitectura Backend de Microservicios             ');
  console.log('================================================================\n');

  // 1. Verificación obligatoria de conectividad con PostgreSQL
  console.log('[1/3] Verificando conexión a PostgreSQL...');
  const isDbAvailable = await testConnection();

  if (!isDbAvailable) {
    console.error('\n================================================================');
    console.error('❌ ERROR CRÍTICO: No se pudo conectar a la base de datos PostgreSQL');
    console.error('================================================================');
    console.error('El sistema AgroSense no puede iniciar sin persistencia de datos.');
    console.error(`Host configurado: ${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || '5432'}`);
    console.error(`Base de datos:    ${process.env.DB_NAME || 'agrosense'}`);
    console.error(`Usuario:          ${process.env.DB_USER || 'postgres'}\n`);
    console.error('PASOS PARA CORREGIR EN WINDOWS:');
    console.error('1. Verifica que el servicio de PostgreSQL esté en ejecución:');
    console.error('   - Abre "Servicios" (services.msc) y busca "postgresql-x64-XX", o ejecuta en PowerShell como Administrador:');
    console.error('     Start-Service postgresql*');
    console.error('2. Verifica que la base de datos "agrosense" exista:');
    console.error('   - En pgAdmin o con psql: CREATE DATABASE agrosense;');
    console.error('3. Revisa tus credenciales en el archivo .env (copiado desde .env.example).');
    console.error('================================================================\n');
    process.exit(1);
  }
  console.log('✔ Conexión con PostgreSQL establecida exitosamente.\n');

  // 2. Inicialización de Esquemas y Tablas (Idempotente)
  console.log('[2/3] Verificando esquemas y tablas de la base de datos...');
  try {
    await initializeDatabase();
    console.log('✔ Esquemas e índices validados.\n');
  } catch (error) {
    console.error('❌ Error al inicializar tablas en PostgreSQL:', error.message);
    process.exit(1);
  }

  // 3. Inicio coordinado de los 4 Microservicios y el API Gateway
  console.log('[3/3] Iniciando microservicios y API Gateway...\n');
  try {
    const gestionPort = parseInt(process.env.GESTION_PORT || '3001', 10);
    const sensorPort = parseInt(process.env.SENSOR_PORT || '3002', 10);
    const medicionPort = parseInt(process.env.MEDICION_PORT || '3003', 10);
    const alertaPort = parseInt(process.env.ALERTA_PORT || '3004', 10);
    const gatewayPort = parseInt(process.env.GATEWAY_PORT || '8080', 10);

    const s1 = await startGestion(gestionPort);
    runningServers.push({ name: 'GestionService', server: s1 });

    const s2 = await startSensor(sensorPort);
    runningServers.push({ name: 'SensorService', server: s2 });

    const s3 = await startMedicion(medicionPort);
    runningServers.push({ name: 'MedicionService', server: s3 });

    const s4 = await startAlerta(alertaPort);
    runningServers.push({ name: 'AlertaService', server: s4 });

    const sGw = await startGateway(gatewayPort);
    runningServers.push({ name: 'API Gateway', server: sGw });

    console.log('\n================================================================');
    console.log('✔ TOPOLOGÍA DE MICROSERVICIOS OPERATIVA');
    console.log('================================================================');
    console.log(` • API Gateway:      http://localhost:${gatewayPort} (PUNTO DE ENTRADA PÚBLICO)`);
    console.log(` • GestionService:   http://localhost:${gestionPort} (Productores y Cultivos)`);
    console.log(` • SensorService:    http://localhost:${sensorPort} (Sensores)`);
    console.log(` • MedicionService:  http://localhost:${medicionPort} (Mediciones y Reglas)`);
    console.log(` • AlertaService:    http://localhost:${alertaPort} (Alertas)`);
    console.log(` • Health Cluster:   http://localhost:${gatewayPort}/health`);
    console.log('================================================================');
    console.log('Presiona Ctrl+C en cualquier momento para detener todos los servicios de forma limpia.\n');
  } catch (error) {
    console.error('❌ Error durante el arranque de los servicios:', error.message);
    await shutdown('ERROR_STARTUP');
  }
}

main().catch(async (err) => {
  console.error('❌ Error fatal:', err);
  process.exit(1);
});
