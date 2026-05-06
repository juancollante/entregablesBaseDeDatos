package com.logistica.logistica_notificaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notificacionesOpenAPI(@Value("${openapi.server.url:}") String publicBaseUrl) {
        return new OpenAPI()
                .info(
                        OpenApiDocSupport.info(
                                "logistica-notificaciones",
                                "API HTTP mínima (salud). Kafka, WebSocket y correo no se exponen como REST en este documento."))
                .servers(List.of(OpenApiDocSupport.deployedServer(publicBaseUrl)));
    }
}
