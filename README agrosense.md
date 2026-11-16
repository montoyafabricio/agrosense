# AgroSense 🌱

> Sistema de agricultura inteligente basado en una arquitectura backend de microservicios para la telemetría y monitoreo de parcelas agrícolas.

---

## 📋 Descripción

**AgroSense** centraliza la ingesta y validación de datos telemétricos provenientes de sensores en campo (humedad, temperatura, etc.), evalúa reglas agronómicas en tiempo real y dispara alertas automatizadas para prevenir daños en cultivos.

---

## 🏗️ Arquitectura del Sistema

El sistema implementa una arquitectura desacoplada coordinada por un **API Gateway**:

| Componente | Puerto | Responsabilidad |
|---|---|---|
| **API Gateway** | `8080` | Punto de entrada único, enrutamiento proxy, manejo de timeouts y códigos de error unificados. |
| **Gestión Service** | `3001` | Administración de productores, parcelas y umbrales de cultivos. |
| **Sensor Service** | `3002` | Registro, vinculación y estado operativo de sensores IoT. |
| **Medición Service** | `3003` | Recepción de telemetría, validación de esquemas (Zod) y persistencia temporal. |
| **Alerta Service** | `3004` | Evaluación de reglas de negocio y generación de incidentes/alertas. |
| **PostgreSQL** | `5432` | Base de datos relacional con particionamiento y restricciones de integridad. |

---

## 🛠️ Stack Tecnológico

- **Runtime:** Node.js (v20+ / v24, ESM)
- **Framework Web:** Express 5
- **Base de Datos:** PostgreSQL con driver nativo `pg`
- **Validación de Datos:** Zod
- **Testing:** Node.js Native Test Runner (`node --test`)

---

## 📂 Estructura del Proyecto

```text
agrosense/
├── database/           # Conexión, pool y scripts SQL de inicialización
├── gateway/            # API Gateway y enrutador proxy
├── services/
│   ├── alerta/         # Microservicio de alertas y notificaciones
│   ├── gestion/        # Microservicio de productores y cultivos
│   ├── medicion/       # Microservicio de ingesta de telemetría
│   ├── sensor/         # Microservicio de catálogo de sensores
│   └── shared/         # DTOs, validaciones Zod, utilidades y reglas
├── test/               # Pruebas unitarias y de integración
├── demo.js             # Simulación automatizada de flujo completo
├── start.js            # Orquestador de arranque de todos los microservicios
├── documentacion.html  # Informe técnico detallado
└── package.json
```

---

## 🚀 Puesta en Marcha

### 1. Prerrequisitos
- Node.js (>= 20.x)
- PostgreSQL corriendo localmente o en contenedor

### 2. Instalación y Configuración

```bash
# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env
```

Edita `.env` con tus credenciales de PostgreSQL:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=agrosense
DB_USER=postgres
DB_PASSWORD=tu_password
```

### 3. Inicializar Base de Datos

Crea las tablas, índices y datos iniciales:
```bash
npm run db:init
```

### 4. Iniciar el Sistema

Levanta todos los microservicios y el API Gateway en un solo comando:
```bash
npm start
```

---

## 🧪 Pruebas y Demostración

```bash
# Ejecutar todas las pruebas (unitarias e integración)
npm test

# Ejecutar solo pruebas unitarias
npm run test:unit

# Ejecutar simulación de flujo completo (productores, sensores, mediciones y alertas)
npm run demo
```

---

## 📡 Endpoints Principales (vía API Gateway `:8080`)

| Módulo | Ruta | Método | Descripción |
|---|---|---|---|
| **Productores** | `/api/productores` | `GET` / `POST` | Listar y crear productores agrícolas |
| **Cultivos** | `/api/cultivos` | `GET` / `POST` | Gestionar cultivos y parámetros óptimos |
| **Sensores** | `/api/sensores` | `GET` / `POST` | Registro y consulta de dispositivos de campo |
| **Mediciones** | `/api/mediciones` | `GET` / `POST` | Envío de telemetría y consulta histórica |
| **Alertas** | `/api/alertas` | `GET` / `PATCH` | Monitoreo y resolución de alertas activas |

---

## 📄 Documentación Técnica

El repositorio incluye un informe técnico exhaustivo con diagramas y decisiones de diseño en [documentacion.html](documentacion.html) (listo para previsualizar o exportar a PDF con `Ctrl + P`).
