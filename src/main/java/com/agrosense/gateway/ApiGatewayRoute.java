package com.agrosense.gateway;

public record ApiGatewayRoute(
    String pathPrefix,
    String moduleName,
    String domainSchema,
    String description
) {
}
