package com.logistica.logistica_seguimiento.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI seguimientoOpenAPI(@Value("${openapi.server.url:}") String publicBaseUrl) {
        return new OpenAPI()
                .info(
                        OpenApiDocSupport.info(
                                "logistica-seguimiento",
                                "Historial público por guía, catálogo de sedes y registro de eventos (OPERADOR/ADMIN con JWT).",
                                true))
                .servers(List.of(OpenApiDocSupport.deployedServer(publicBaseUrl)))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        OpenApiDocSupport.JWT_SCHEME, OpenApiDocSupport.bearerJwt()));
    }
}
