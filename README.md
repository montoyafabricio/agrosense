# AgroSense — Sistema de Agricultura Inteligente

Backend completo, modular y funcional para **AgroSense**, un sistema de agricultura inteligente basado en una arquitectura orientada a **Microservicios**, **API Gateway**, validación rigurosa mediante **DTO (Zod)** y persistencia en **PostgreSQL**.

---

## 1. ¿Qué es AgroSense y cuál es su objetivo?

**AgroSense** es una plataforma tecnológica diseñada para el monitoreo agronómico y la gestión de cultivos en tiempo real. Su objetivo principal es centralizar la administración de productores agrícolas y sus parcelas de cultivo, recopilar mediciones telemétricas de sensores ambientales (temperatura y humedad) y evaluar de forma automática reglas agronómicas para emitir alertas tempranas preventivas (riesgo de estrés hídrico, exceso de humedad o estrés térmico).

---

## 2. Las Cinco Entidades de Negocio (Fieles al Diagrama de Clases)

El sistema implementa de forma estricta y exclusiva las 5 entidades definidas en el diagrama de clases original:

| Entidad | Atributos de Negocio | Descripción |
| :--- | :--- | :--- |
| **`Productor`** | `id` (UUID), `nombre` (String), `contacto` (String) | Persona o entidad responsable de uno o más cultivos. |
| **`Cultivo`** | `id` (UUID), `especie` (String), `superficie` (Double > 0 ha), `fechaSiembra` (YYYY-MM-DD), `productorId` (UUID) | Parcela agrícola administrada por un productor. |
| **`Sensor`** | `id` (UUID), `tipo` (`humedad`, `temperatura`, `mixto`), `ubicacion` (String), `estado` (`activo`, `inactivo`, `mantenimiento`), `cultivoId` (UUID) | Dispositivo telemétrico instalado en un cultivo. |
| **`Medicion`** | `id` (UUID), `temperatura` (Double °C), `humedad` (Double [0, 100]%), `fecha` (ISO 8601), `sensorId` (UUID) | Registro histórico e inmutable de variables agroclimáticas. |
| **`Alerta`** | `id` (UUID), `tipo` (String), `descripcion` (String), `prioridad` (`baja`, `media`, `alta`), `fecha` (ISO 8601), `cultivoId` (UUID) | Notificación técnica asociada a un cultivo. **No contiene `medicionId`** para respetar el diagrama de clases original. |

> **Nota sobre identificadores**: Todos los `id` son generados automáticamente por el servidor mediante UUID v4. El cliente no puede suministrar ni manipular los IDs al crear recursos.

---

## 3. Relaciones del Diagrama de Clases

* **Productor — Cultivo**: Un Productor administra cero o muchos Cultivos (`1` a `0..*`). Cada Cultivo pertenece obligatoriamente a un Productor.
* **Cultivo — Sensor**: Un Cultivo posee cero o muchos Sensores (`1` a `0..*`). Cada Sensor está instalado en un Cultivo.
* **Sensor — Medicion**: Un Sensor registra cero o muchas Mediciones (`1` a `0..*`). Cada Medición pertenece a un Sensor.
* **Cultivo — Alerta**: Un Cultivo recibe cero o muchas Alertas (`1` a `0..*`). Cada Alerta está vinculada a un Cultivo.
* **Medicion — Alerta**: Relación discontinua de dependencia. Las mediciones desencadenan la creación de alertas que se vinculan al cultivo del sensor medido, sin inventar atributos artificiales como `medicionId` en la entidad Alerta.

---

## 4. Arquitectura del Sistema (4 Microservicios + 1 API Gateway)

```
                            [ Cliente / Postman / Frontend ]
                                          │
                                   HTTP :8080 (/api/*)
                                          ▼
                         ┌─────────────────────────────────┐
                         │           API GATEWAY           │
                         │          (Puerto 8080)          │
                         │   • Enrutamiento Inverso        │
                         │   • Health Global y 503 Handler │
                         └─────────────────────────────────┘
                                          │
        ┌───────────────────┬─────────────┴───────┬───────────────────┐
        │ :3001             │ :3002               │ :3003             │ :3004
        ▼                   ▼                     ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌───────────────────┐ ┌─────────────────┐
│ GestionService  │ │  SensorService  │ │  MedicionService  │ │  AlertaService  │
│  (Productores   │ │    (Sensores)   │ │  (Mediciones y    │ │    (Alertas)    │
│   y Cultivos)   │ │                 │ │      Reglas)      │ │                 │
└─────────────────┘ └─────────────────┘ └───────────────────┘ └─────────────────┘
        │                   │                     │                   │
        │ Esquema gestion   │ Esquema sensores    │ Esquema mediciones│ Esquema alertas
        └───────────────────┴─────────────┬───────┴───────────────────┘
                                          ▼
                         ┌─────────────────────────────────┐
                         │      PostgreSQL (agrosense)     │
                         │  • Separación lógica (Schemas)  │
                         │  • Consultas parametrizadas     │
                         └─────────────────────────────────┘
```

### Responsabilidades por Componente

1. **API Gateway (Puerto `8080`)**:
   * Único punto de acceso público.
   * Enruta hacia los microservicios preservando métodos, headers, query params y body.
   * Maneja errores de comunicación devolviendo `HTTP 503 Service Unavailable` si algún microservicio está inactivo.
   * No contiene reglas de negocio ni accede a PostgreSQL.

2. **Microservicio 1: `GestionService` (Puerto `3001`)**:
   * Responsable de `Productor` y `Cultivo`.
   * Restricción: No permite eliminar productores con cultivos asociados (`409 Conflict`).
   * Restricción: No permite eliminar cultivos con sensores o alertas asociadas (consulta por HTTP a `SensorService` y `AlertaService`).

3. **Microservicio 2: `SensorService` (Puerto `3002`)**:
   * Responsable de `Sensor`.
   * Verifica por HTTP con `GestionService` que el `cultivoId` exista antes de crear o actualizar.
   * Restricción: No permite eliminar sensores con mediciones registradas (consulta por HTTP a `MedicionService`).

4. **Microservicio 3: `MedicionService` (Puerto `3003`)**:
   * Responsable de `Medicion` (registro histórico, sin PUT ni DELETE).
   * Verifica por HTTP que el sensor exista y esté en estado `activo`.
   * Persiste la medición y evalúa las **reglas automáticas**.
   * Notifica por HTTP a `AlertaService`.
   * Si `AlertaService` falla, la medición **no se elimina**: se responde `HTTP 201` con estado `pendiente` y permite reintentos mediante `POST /api/mediciones/:id/procesar`.

5. **Microservicio 4: `AlertaService` (Puerto `3004`)**:
   * Responsable de `Alerta` (registro histórico, sin PUT ni DELETE).
   * Verifica por HTTP con `GestionService` la existencia del cultivo.
   * Previene duplicación de alertas ante reintentos de procesamiento.

---

## 5. DTO y Validaciones con Zod

Cada microservicio implementa validaciones estrictas con Zod:
* **Separación de entrada y salida**: Los DTO de entrada rechazan campos no autorizados con `.strict()` (por ejemplo, el cliente no puede enviar su propio `id`).
* **Operaciones PUT completas**: Requieren el conjunto completo de campos editables (no se permite PATCH).
* **Validaciones estrictas de dominio**:
  * `temperatura`: Número finito en °C.
  * `humedad`: Número acotado entre 0 y 100%.
  * `superficie`: Número positivo en hectáreas (> 0).
  * `fechaSiembra`: Formato YYYY-MM-DD y fecha de calendario válida.
  * `fecha`: Formato ISO 8601 con zona horaria.
  * Enums: `tipo` de sensor (`humedad`, `temperatura`, `mixto`), `estado` (`activo`, `inactivo`, `mantenimiento`), `prioridad` (`baja`, `media`, `alta`).

---

## 6. Persistencia en PostgreSQL

El proyecto utiliza una única base de datos PostgreSQL llamada `agrosense`, dividida en **4 esquemas lógicos** para aislar los límites de cada microservicio:
* `gestion`: tablas `productores` y `cultivos`.
* `sensores`: tabla `sensores`.
* `mediciones`: tablas `mediciones` y `mediciones_procesamiento`.
* `alertas`: tabla `alertas`.

> **Importante**: No existen claves foráneas (`FOREIGN KEY`) entre esquemas pertenecientes a microservicios distintos. Toda validación interservicios se ejecuta mediante peticiones HTTP con `fetch` nativo.

---

## 7. Reglas Automáticas de Alertas

Centralizadas en `services/medicion/rules.js`:

| Regla | Condición Estricta | Tipo Alerta | Descripción | Prioridad |
| :--- | :--- | :--- | :--- | :--- |
| **1. Humedad baja** | `humedad < 30.0` | `RIEGO` | "Revisar la necesidad de riego." | `alta` |
| **2. Humedad alta** | `humedad > 80.0` | `EXCESO_HUMEDAD` | "Revisar el drenaje antes de regar." | `media` |
| **3. Temperatura alta** | `temperatura > 35.0` | `CALOR` | "Revisar las condiciones del cultivo por temperatura elevada." | `alta` |

* **Comparaciones estrictas**: Los valores límite exactos (30.0, 80.0 y 35.0) no disparan alertas.
* **Múltiples alertas**: Una medición con `temperatura = 38` y `humedad = 20` genera simultáneamente `RIEGO` y `CALOR`.

---

## 8. Puertos y Variables de Entorno

Archivo `.env` en la raíz del proyecto:

```ini
DB_HOST=localhost
DB_PORT=5432
DB_NAME=agrosense
DB_USER=postgres
DB_PASSWORD=TU_PASSWORD_AQUI

DB_NAME_TEST=agrosense_test

GATEWAY_PORT=8080
GESTION_PORT=3001
SENSOR_PORT=3002
MEDICION_PORT=3003
ALERTA_PORT=3004

GESTION_SERVICE_URL=http://localhost:3001
SENSOR_SERVICE_URL=http://localhost:3002
MEDICION_SERVICE_URL=http://localhost:3003
ALERTA_SERVICE_URL=http://localhost:3004
```

---

## 9. Endpoints de la API (Acceso por Gateway :8080)

### Salud del Sistema
* `GET http://localhost:8080/health` (Estado del Gateway y los 4 microservicios)

### Productores
* `GET /api/productores` — Listar todos los productores
* `POST /api/productores` — Crear un productor
* `GET /api/productores/:id` — Consultar un productor por ID
* `PUT /api/productores/:id` — Actualizar un productor
* `DELETE /api/productores/:id` — Eliminar productor (solo si no tiene cultivos)

### Cultivos
* `GET /api/cultivos` — Listar todos los cultivos
* `POST /api/cultivos` — Crear un cultivo
* `GET /api/cultivos/:id` — Consultar un cultivo por ID
* `PUT /api/cultivos/:id` — Actualizar un cultivo
* `DELETE /api/cultivos/:id` — Eliminar cultivo (solo si no tiene sensores ni alertas)

### Sensores
* `GET /api/sensores` — Listar sensores (Filtro opcional: `?cultivoId=UUID`)
* `POST /api/sensores` — Registrar sensor
* `GET /api/sensores/:id` — Consultar sensor por ID
* `PUT /api/sensores/:id` — Actualizar sensor
* `DELETE /api/sensores/:id` — Eliminar sensor (solo si no tiene mediciones)

### Mediciones
* `GET /api/mediciones` — Listar mediciones (Filtro opcional: `?sensorId=UUID`)
* `POST /api/mediciones` — Registrar medición y disparar reglas automáticas
* `GET /api/mediciones/:id` — Consultar medición por ID
* `POST /api/mediciones/:id/procesar` — Reintentar el procesamiento de alertas pendientes

### Alertas
* `GET /api/alertas` — Listar alertas (Filtros: `?cultivoId=UUID`, `?prioridad=alta|media|baja`)
* `POST /api/alertas` — Registrar alerta manual
* `GET /api/alertas/:id` — Consultar alerta por ID

---

## 10. Instalación y Ejecución en Windows

### Paso 1: Configurar PostgreSQL
1. Abre pgAdmin o tu terminal `psql` y crea la base de datos:
   ```sql
   CREATE DATABASE agrosense;
   ```
2. Si deseas ejecutar las pruebas de integración en una base aislada:
   ```sql
   CREATE DATABASE agrosense_test;
   ```

### Paso 2: Configurar Variables de Entorno
Copia el archivo `.env.example` como `.env` y coloca tu contraseña de PostgreSQL:
```powershell
Copy-Item .env.example .env
```

### Paso 3: Instalar Dependencias
```powershell
npm install
```

### Paso 4: Iniciar el Sistema Completo
```powershell
npm start
```
*`start.js` verificará primero que PostgreSQL esté activo. Si no lo está, te indicará claramente cómo iniciarlo. Luego creará los esquemas y tablas automáticamente e iniciará los 4 microservicios y el API Gateway en un solo comando.*

---

## 11. Ejemplos Completos para Postman

> **Aclaración**: Los identificadores con formato `UUID_REAL_...` son marcadores y deben reemplazarse por los IDs devueltos por las respuestas de la API.

### 1. Crear Productor
* **Método**: `POST`
* **URL**: `http://localhost:8080/api/productores`
* **Body (JSON)**:
```json
{
  "nombre": "Ana Pérez",
  "contacto": "ana@example.com"
}
```

### 2. Crear Cultivo
* **Método**: `POST`
* **URL**: `http://localhost:8080/api/cultivos`
* **Body (JSON)**:
```json
{
  "especie": "Tomate",
  "superficie": 2.5,
  "fechaSiembra": "2026-10-01",
  "productorId": "UUID_REAL_DEL_PRODUCTOR"
}
```

### 3. Crear Sensor
* **Método**: `POST`
* **URL**: `http://localhost:8080/api/sensores`
* **Body (JSON)**:
```json
{
  "tipo": "mixto",
  "ubicacion": "Parcela A",
  "estado": "activo",
  "cultivoId": "UUID_REAL_DEL_CULTIVO"
}
```

### 4. Registrar Medición (Dispara RIEGO y CALOR)
* **Método**: `POST`
* **URL**: `http://localhost:8080/api/mediciones`
* **Body (JSON)**:
```json
{
  "temperatura": 38.0,
  "humedad": 20.0,
  "fecha": "2026-11-05T01:00:00Z",
  "sensorId": "UUID_REAL_DEL_SENSOR"
}
```
*Respuesta esperada: `HTTP 201 Created` con la medición registrada y dos alertas generadas: `RIEGO` (prioridad alta) y `CALOR` (prioridad alta).*

### 5. Crear Alerta Manualmente
* **Método**: `POST`
* **URL**: `http://localhost:8080/api/alertas`
* **Body (JSON)**:
```json
{
  "tipo": "INSPECCION_MANUAL",
  "descripcion": "Revisión preventiva de plagas en cultivo de tomate",
  "prioridad": "media",
  "fecha": "2026-11-05T02:00:00Z",
  "cultivoId": "UUID_REAL_DEL_CULTIVO"
}
```

---

## 12. Pruebas Automatizadas y Demostración

El proyecto incluye suites completas con el ejecutor nativo de Node.js (`node:test` y `node:assert`):

* **Ejecutar todas las pruebas**:
  ```powershell
  npm test
  ```
* **Ejecutar pruebas unitarias de reglas y DTO**:
  ```powershell
  npm run test:unit
  ```
* **Ejecutar la demostración integral en vivo**:
  ```powershell
  npm run demo
  ```

---

## 13. Limitaciones Técnicas del Prototipo

1. **Requerimiento simultáneo de variables en Medicion**:
   Para este prototipo académico, toda `Medicion` exige valores numéricos tanto para `temperatura` como para `humedad`, incluso si el sensor físico fue catalogado como de un solo tipo (`temperatura` o `humedad`).
2. **Ausencia de Transacciones Distribuidas**:
   No existe un coordinador de transacciones distribuidas (como Two-Phase Commit o Sagas complejas). La consistencia ante fallos parciales se gestiona mediante compensación lógica: si la notificación de alertas falla, la medición se preserva con estado `pendiente` y se provee el endpoint de reintento idempotente `POST /api/mediciones/:id/procesar`.
3. **Umbrales Demostrativos**:
   Los valores límite (< 30% humedad, > 80% humedad, > 35°C temperatura) son puramente didácticos y demostrativos. No controlan actuadores físicos ni válvulas en campo.

---

## 14. Guion para la Defensa Oral del Proyecto

Durante la exposición o defensa académica ante los profesores, puedes estructurar tu presentación en 4 partes:

1. **Introducción y Dominio (1 minuto)**:
   > *"AgroSense es una solución de agricultura de precisión orientada a microservicios. Nos basamos estrictamente en el diagrama de clases original con cinco entidades: Productor, Cultivo, Sensor, Medición y Alerta. Su objetivo es recolectar datos climáticos y generar alertas tempranas automáticas."*

2. **Arquitectura y Aislamiento (2 minutos)**:
   > *"Implementamos cuatro microservicios de dominio (Gestión, Sensor, Medición y Alertas) y un API Gateway en el puerto 8080 como único punto de entrada público. Cada microservicio es dueño de su propio esquema en PostgreSQL (`gestion`, `sensores`, `mediciones`, `alertas`). Respetamos el principio de microservicios: no existen JOINs ni foreign keys entre microservicios; todas las relaciones entre dominios se validan mediante peticiones HTTP asíncronas con el fetch nativo de Node.js."*

3. **Lógica de Negocio y Resiliencia (2 minutos)**:
   > *"Cuando un sensor activo envía una medición de temperatura y humedad, el servicio de Mediciones evalúa tres reglas estrictas: humedad baja (< 30) dispara alerta de Riego, humedad alta (> 80) exceso de humedad, y calor (> 35) alerta térmica. Como en el diagrama original Alerta no tiene `medicionId`, garantizamos la idempotencia mediante control técnico en el servicio de mediciones y verificación en alertas. Si el servicio de alertas estuviese caído al registrar la medición, aplicamos tolerancia a fallos parciales: la medición nunca se pierde, se almacena en estado pendiente y se puede reprocesar mediante su endpoint de reintento."*

4. **Calidad y Demostración (1 minuto)**:
   > *"Todo el flujo de entrada y salida está protegido mediante DTOs estrictos con Zod, impidiendo campos desconocidos o asignación manual de IDs. Contamos con pruebas unitarias para todas las condiciones de borde y un script de demostración interactivo que exhibe el ciclo de vida completo."*
