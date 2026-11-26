# AgroSense 🌱 (Spring Boot & Java 21 LTS)

> Sistema de agricultura inteligente basado en una arquitectura backend modular en **Spring Boot 3** y **Java 21 (LTS)** para la telemetría, monitoreo y reglas agronómicas automatizadas sobre parcelas agrícolas.

---

## 📋 Descripción

**AgroSense** centraliza la ingesta y validación de datos telemétricos provenientes de sensores en campo (humedad y temperatura), evalúa reglas agronómicas en tiempo real y dispara alertas automatizadas con control de idempotencia para prevenir daños en cultivos.

El proyecto está desarrollado completamente en **Java 21 (LTS)**, utilizando **Spring Boot 3**, **Spring Data JPA**, **Jakarta Bean Validation (DTOs)**, **PostgreSQL** con múltiples esquemas de base de datos (`gestion`, `sensores`, `mediciones`, `alertas`) y pruebas exhaustivas con **JUnit 5**.

---

## 🏗️ Arquitectura del Sistema

El sistema implementa una arquitectura desacoplada por módulos de dominio bajo un servidor unificado Spring Boot:

| Módulo | Ruta Base | Responsabilidad |
|---|---|---|
| **Gestión** | `/api/productores`, `/api/cultivos` | Administración de productores agrícolas y cultivos con validaciones de integridad referencial. |
| **Sensores** | `/api/sensores` | Catálogo, vinculación a cultivos y estado operativo de sensores IoT (`activo`, `inactivo`, `mantenimiento`). |
| **Mediciones** | `/api/mediciones` | Recepción de telemetría, validación de rangos, persistencia temporal y evaluación de reglas agronómicas. |
| **Alertas** | `/api/alertas` | Generación de incidentes/alertas con control de idempotencia (ventana temporal de 120s) y filtro por prioridad. |
| **Salud** | `/health` | Diagnóstico de disponibilidad del servicio y sus componentes. |
| **PostgreSQL** | `:5432` | Base de datos relacional con partición lógica en 4 esquemas (`gestion`, `sensores`, `mediciones`, `alertas`). |

---

## 🛠️ Stack Tecnológico

- **Runtime:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.0
- **Persistencia:** Spring Data JPA / Hibernate
- **Base de Datos:** PostgreSQL (Driver nativo `org.postgresql:postgresql`)
- **Validación de Datos:** Jakarta Bean Validation (`spring-boot-starter-validation`) con Java Records como DTOs
- **Documentación de API:** OpenAPI 3 & Swagger UI (`springdoc-openapi`)
- **Testing:** JUnit 5 (Jupiter), Mockito, MockMvc, H2 Database para pruebas aisladas
- **Build Tool:** Apache Maven & Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## 📂 Estructura del Proyecto

El repositorio cuenta tanto con el **Backend Modular Principal** (Spring Boot unificado en `src/`) como con los **Módulos de Microservicios Independientes** correspondientes:

```text
agrosense/
├── pom.xml                                      # Configuración Maven raíz (Java 21 y Spring Boot 3)
├── mvnw / mvnw.cmd                              # Maven Wrapper para compilación sin requerir Maven instalado
├── .mvn/wrapper/                                # Configuración del Wrapper de Maven
├── database/
│   └── init.sql                                 # Script SQL DDL de inicialización de esquemas y tablas
├── src/                                         # Backend Modular Principal (Spring Boot)
│   ├── main/
│   │   ├── java/com/agrosense/
│   │   │   ├── AgroSenseApplication.java        # Punto de entrada principal Spring Boot
│   │   │   ├── config/                          # Configuración CORS y Web (WebConfig.java)
│   │   │   ├── controller/                      # Controladores REST (incluye HealthController en /health)
│   │   │   ├── dto/                             # Data Transfer Objects (Records) con validaciones estrictas
│   │   │   ├── exception/                       # Manejador global de excepciones y respuestas de error
│   │   │   ├── model/                           # Entidades JPA y Enums vinculados a PostgreSQL
│   │   │   ├── repository/                      # Repositorios Spring Data JPA
│   │   │   └── service/                         # Lógica de negocio y motor de reglas agronómicas
│   │   └── resources/
│   │       ├── application.yml                  # Configuración de PostgreSQL y aplicación
│   │       ├── schema.sql                       # DDL de PostgreSQL
│   │       └── db/migration/                    # Migraciones Flyway
│   └── test/                                    # Pruebas unitarias y de integración
├── api-gateway/                                 # API Gateway independiente (Spring Cloud Gateway :8080)
│   └── src/main/java/com/agrosense/gateway/
│       ├── config/CorsConfig.java               # Configuración CORS reactiva
│       └── controller/HealthController.java     # Endpoint /health
├── gestion-service/                             # Microservicio de Productores y Cultivos (:3001)
├── sensor-service/                              # Microservicio de Sensores IoT (:3002)
├── medicion-service/                            # Microservicio de Ingesta y Reglas (:3003)
├── alerta-service/                              # Microservicio de Alertas e Incidentes (:3004)
└── README.md
```

---

## 🚀 Puesta en Marcha

### 1. Prerrequisitos
- **Java Development Kit (JDK) 21** (LTS) instalado y configurado en `JAVA_HOME`.
- **PostgreSQL** corriendo localmente en el puerto `5432` con la base de datos `agrosense`.

### 2. Configurar Base de Datos PostgreSQL

Puedes usar las credenciales por defecto o definir variables de entorno:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=agrosense
DB_USER=postgres
DB_PASSWORD=tu_password
SERVER_PORT=8080
```

Al arrancar la aplicación, Spring Boot ejecutará automáticamente `schema.sql` para inicializar los 4 esquemas (`gestion`, `sensores`, `mediciones`, `alertas`) y sus tablas e índices.

### 3. Compilación y Ejecución

Utiliza el Maven Wrapper incluido:

```bash
# En Windows:
.\mvnw.cmd spring-boot:run

# En Linux/macOS:
./mvnw spring-boot:run
```

O si tienes `mvn` en tu PATH:
```bash
mvn spring-boot:run
```

---

## 🧪 Pruebas Unitarias (JUnit 5)

Ejecuta todas las pruebas unitarias y de controladores con:

```bash
.\mvnw.cmd test
```

### Cobertura de pruebas unitarias:
1. **`AgronomicRulesTest`**: Evaluación estricta de umbrales agronómicos demostrativos:
   - Humedad < 30% -> `RIEGO` (alta).
   - Humedad > 80% -> `EXCESO_HUMEDAD` (media).
   - Temperatura > 35°C -> `CALOR` (alta).
   - Combinaciones simultáneas y valores de borde (30%, 80%, 35°C).
2. **`DtoValidationTest`**: Verificación de validaciones en DTOs (`@NotBlank`, `@Positive`, `@DecimalMin`, `@DecimalMax`, rangos de humedad 0-100%, etc.).
3. **`ProductorServiceTest`**: Operaciones CRUD y protección de integridad referencial (no eliminar productor con cultivos).
4. **`CultivoServiceTest`**: Validaciones de productor existente y restricciones para evitar huérfanos en sensores y alertas.
5. **`SensorServiceTest`**: Validaciones de cultivo existente y restricción de eliminación si tiene mediciones históricas.
6. **`MedicionServiceTest`**: Ingesta telemétrica, rechazo de sensores inactivos, disparo de alertas y estado de procesamiento.
7. **`AlertaServiceTest`**: Control de idempotencia (detección de alertas duplicadas en ventana de 120s).
8. **`ProductorControllerTest` & `MedicionControllerTest`**: Pruebas de integración de capa web (`MockMvc`).

---

## 📡 Endpoints de la API

| Módulo | Método | Endpoint | Descripción |
|---|---|---|---|
| **Salud** | `GET` | `/health` | Estado del backend y componentes |
| **Productores** | `GET` | `/api/productores` | Listar todos los productores |
| **Productores** | `POST` | `/api/productores` | Crear un productor (`nombre`, `contacto`) |
| **Productores** | `GET` | `/api/productores/{id}` | Obtener productor por ID |
| **Productores** | `PUT` | `/api/productores/{id}` | Actualizar productor |
| **Productores** | `DELETE` | `/api/productores/{id}` | Eliminar productor (valida sin cultivos) |
| **Cultivos** | `GET` | `/api/cultivos` | Listar cultivos |
| **Cultivos** | `POST` | `/api/cultivos` | Registrar cultivo (`especie`, `superficie`, `fechaSiembra`, `productorId`) |
| **Cultivos** | `GET` | `/api/cultivos/{id}` | Obtener cultivo por ID |
| **Cultivos** | `PUT` | `/api/cultivos/{id}` | Actualizar cultivo |
| **Cultivos** | `DELETE` | `/api/cultivos/{id}` | Eliminar cultivo (valida sin sensores ni alertas) |
| **Sensores** | `GET` | `/api/sensores` | Listar sensores (filtro opcional: `?cultivoId=...`) |
| **Sensores** | `POST` | `/api/sensores` | Registrar sensor (`tipo`, `ubicacion`, `estado`, `cultivoId`) |
| **Sensores** | `GET` | `/api/sensores/{id}` | Obtener sensor por ID |
| **Sensores** | `PUT` | `/api/sensores/{id}` | Actualizar sensor |
| **Sensores** | `DELETE` | `/api/sensores/{id}` | Eliminar sensor (valida sin mediciones históricas) |
| **Mediciones** | `GET` | `/api/mediciones` | Listar mediciones (filtro opcional: `?sensorId=...`) |
| **Mediciones** | `POST` | `/api/mediciones` | Registrar telemetría y disparar evaluación de reglas |
| **Mediciones** | `POST` | `/api/mediciones/{id}/procesar` | Reintentar sincronización de alertas |
| **Alertas** | `GET` | `/api/alertas` | Listar alertas (filtros: `?cultivoId=...`, `?prioridad=...`) |
| **Alertas** | `POST` | `/api/alertas` | Crear alerta manual con control de idempotencia |
| **Swagger UI** | `GET` | `/swagger-ui.html` | Documentación interactiva OpenAPI |
