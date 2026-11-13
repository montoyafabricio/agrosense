# AgroSense — Sistema de Agricultura Inteligente

Backend con arquitectura de Microservicios, API Gateway (puerto 8080), validación con DTOs (Zod) y persistencia en PostgreSQL.

## Estructura
* **4 Microservicios**: `GestionService` (:3001), `SensorService` (:3002), `MedicionService` (:3003), `AlertaService` (:3004).
* **1 API Gateway**: Puerto 8080 (único punto de entrada público).
* **Persistencia**: PostgreSQL (Esquemas: `gestion`, `sensores`, `mediciones`, `alertas`).

## Ejecución Rápida
```powershell
npm install
node start.js
```

Para ver la documentación completa, diagramas y ejemplos de Postman, consulta [docs/DOCUMENTACION.md](docs/DOCUMENTACION.md).
