# AgroSense — Documentación Completa del Sistema

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

---

## 3. Relaciones del Diagrama de Clases

* **Productor — Cultivo**: Un Productor administra cero o muchos Cultivos (`1` a `0..*`).
* **Cultivo — Sensor**: Un Cultivo posee cero o muchos Sensores (`1` a `0..*`).
* **Sensor — Medicion**: Un Sensor registra cero o muchas Mediciones (`1` a `0..*`).
* **Cultivo — Alerta**: Un Cultivo recibe cero o muchas Alertas (`1` a `0..*`).
* **Medicion — Alerta**: Relación discontinua de dependencia. Las mediciones desencadenan la creación de alertas asociadas al cultivo sin crear atributos ficticios como `medicionId` en Alerta.

---

## 4. Arquitectura de Microservicios (4 Servicios + Gateway)

* **API Gateway (Puerto `8080`)**: Punto único de entrada público, enrutamiento inverso y manejo de errores 503.
* **GestionService (Puerto `3001`)**: Productores y Cultivos.
* **SensorService (Puerto `3002`)**: Sensores ambientales.
* **MedicionService (Puerto `3003`)**: Mediciones históricas y evaluación de reglas.
* **AlertaService (Puerto `3004`)**: Alertas automáticas y manuales con prevención de duplicados.

---

## 5. Reglas Automáticas de Alertas

Centralizadas en `services/medicion/rules.js`:
* **1. Humedad baja**: `humedad < 30.0` -> `RIEGO` (alta).
* **2. Humedad alta**: `humedad > 80.0` -> `EXCESO_HUMEDAD` (media).
* **3. Temperatura alta**: `temperatura > 35.0` -> `CALOR` (alta).

---

## 6. Endpoints de la API (Puerto 8080)

* `GET /health` — Salud del cluster
* `GET /api/productores`, `POST /api/productores`, `GET /api/productores/:id`, `PUT /api/productores/:id`, `DELETE /api/productores/:id`
* `GET /api/cultivos`, `POST /api/cultivos`, `GET /api/cultivos/:id`, `PUT /api/cultivos/:id`, `DELETE /api/cultivos/:id`
* `GET /api/sensores`, `POST /api/sensores`, `GET /api/sensores/:id`, `PUT /api/sensores/:id`, `DELETE /api/sensores/:id`
* `GET /api/mediciones`, `POST /api/mediciones`, `GET /api/mediciones/:id`, `POST /api/mediciones/:id/procesar`
* `GET /api/alertas`, `POST /api/alertas`, `GET /api/alertas/:id`

---

## 7. Ejemplos para Postman

### Crear Productor
```json
POST http://localhost:8080/api/productores
{
  "nombre": "Ana Pérez",
  "contacto": "ana@example.com"
}
```

### Crear Cultivo
```json
POST http://localhost:8080/api/cultivos
{
  "especie": "Tomate",
  "superficie": 2.5,
  "fechaSiembra": "2026-10-01",
  "productorId": "UUID_DEL_PRODUCTOR"
}
```

### Crear Sensor
```json
POST http://localhost:8080/api/sensores
{
  "tipo": "mixto",
  "ubicacion": "Parcela A",
  "estado": "activo",
  "cultivoId": "UUID_DEL_CULTIVO"
}
```

### Registrar Medición Crítica (Genera alertas de RIEGO y CALOR)
```json
POST http://localhost:8080/api/mediciones
{
  "temperatura": 38.0,
  "humedad": 20.0,
  "fecha": "2026-11-05T01:00:00Z",
  "sensorId": "UUID_DEL_SENSOR"
}
```

---

## 8. Guion para la Defensa Oral del Proyecto

1. **Introducción y Dominio**: Explicar las 5 entidades del diagrama (Productor, Cultivo, Sensor, Medición, Alerta).
2. **Arquitectura**: 4 microservicios independientes, 1 API Gateway (8080) y PostgreSQL con 4 esquemas (`gestion`, `sensores`, `mediciones`, `alertas`).
3. **Comunicación**: HTTP asíncrono con `fetch` nativo sin cruce de claves foráneas.
4. **Lógica y Resiliencia**: Reglas automáticas (<30, >80, >35), reintentos con tolerancia a fallos parciales e idempotencia.
5. **Validación**: DTOs estrictos con Zod.
