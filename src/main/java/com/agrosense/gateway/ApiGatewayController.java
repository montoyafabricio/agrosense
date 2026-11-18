package com.agrosense.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * AGROSENSE - API GATEWAY MANAGEMENT & TOPOLOGY CONTROLLER
 * ============================================================================
 * Expone la topologia del API Gateway, las rutas a los 4 modulos de dominio
 * y el estado operativo del punto de entrada en el puerto 8080.
 */
@RestController
@RequestMapping("/api/gateway")
public class ApiGatewayController {

    private static final List<ApiGatewayRoute> ROUTES = List.of(
        new ApiGatewayRoute("/api/productores", "GestionService", "gestion", "Gestion de productores agricolas"),
        new ApiGatewayRoute("/api/cultivos", "GestionService", "gestion", "Administracion de parcelas de cultivo"),
        new ApiGatewayRoute("/api/sensores", "SensorService", "sensores", "Catalogo y estado de dispositivos IoT"),
        new ApiGatewayRoute("/api/mediciones", "MedicionService", "mediciones", "Ingesta telemetrica y evaluacion de reglas"),
        new ApiGatewayRoute("/api/alertas", "AlertaService", "alertas", "Gestion e incidentes con control de idempotencia")
    );

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getGatewayRoutes() {
        return ResponseEntity.ok(Map.of(
            "gatewayName", "AgroSense API Gateway",
            "gatewayPort", 8080,
            "status", "ACTIVE",
            "totalRoutes", ROUTES.size(),
            "routes", ROUTES,
            "timestamp", OffsetDateTime.now()
        ));
    }

    @GetMapping("/topology")
    public ResponseEntity<Map<String, Object>> getTopology() {
        return ResponseEntity.ok(Map.of(
            "entrypoint", "http://localhost:8080",
            "protocol", "HTTP/REST",
            "tracingHeader", ApiGatewayFilter.TRACE_HEADER,
            "modules", Map.of(
                "GestionService", Map.of("schema", "gestion", "endpoints", List.of("/api/productores", "/api/cultivos")),
                "SensorService", Map.of("schema", "sensores", "endpoints", List.of("/api/sensores")),
                "MedicionService", Map.of("schema", "mediciones", "endpoints", List.of("/api/mediciones")),
                "AlertaService", Map.of("schema", "alertas", "endpoints", List.of("/api/alertas"))
            ),
            "timestamp", OffsetDateTime.now()
        ));
    }
}
