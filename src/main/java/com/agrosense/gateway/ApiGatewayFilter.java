package com.agrosense.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * ============================================================================
 * AGROSENSE - API GATEWAY REVERSE PROXY & TRACING FILTER
 * ============================================================================
 * Actua como componente perimetral del API Gateway en el puerto 8080:
 * 1. Intercepta todas las peticiones entrantes a /api/* y /health.
 * 2. Inyecta identificadores unicos de correlacion/trazabilidad (X-Gateway-Trace-Id).
 * 3. Mide la latencia de procesamiento de cada solicitud.
 * 4. Aplica politicas uniformes de encabezados HTTP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiGatewayFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayFilter.class);
    public static final String TRACE_HEADER = "X-Gateway-Trace-Id";
    public static final String LATENCY_HEADER = "X-Gateway-Latency-Ms";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // Propaga el identificador de trazabilidad en la respuesta HTTP
        response.setHeader(TRACE_HEADER, traceId);
        response.setHeader("X-Gateway-Server", "AgroSense-API-Gateway-v1.0");

        String method = request.getMethod();
        String path = request.getRequestURI();

        log.debug("[API Gateway] Enrutando solicitud {} {} con TraceId={}", method, path, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            response.setHeader(LATENCY_HEADER, String.valueOf(latency));
            log.debug("[API Gateway] Completado {} {} -> Status {} en {}ms", method, path, response.getStatus(), latency);
        }
    }
}
