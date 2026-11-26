package com.agrosense.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        boolean dbHealthy = false;
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                dbHealthy = conn.isValid(2);
            } catch (Exception e) {
                dbHealthy = false;
            }
        }

        Map<String, Object> components = new HashMap<>();
        components.put("database", dbHealthy ? "UP" : "DOWN");
        components.put("gestion", "UP");
        components.put("sensores", "UP");
        components.put("mediciones", "UP");
        components.put("alertas", "UP");

        return ResponseEntity.ok(Map.of(
            "status", dbHealthy ? "UP" : "DEGRADED",
            "service", "AgroSense Spring Boot Backend",
            "javaVersion", System.getProperty("java.version"),
            "timestamp", OffsetDateTime.now().toString(),
            "components", components
        ));
    }
}
