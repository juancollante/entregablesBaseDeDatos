package com.logistica.logistica_notificaciones.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Textos alineados con el resto de microservicios LogiTrack (OpenAPI 3). Este servicio no expone JWT en REST.
 */
public final class OpenApiDocSupport {

    public static final String VERSION = "0.1";

    private OpenApiDocSupport() {}

    public static Contact contact() {
        return new Contact().name("LogiTrack (EAV06)");
    }

    public static Info info(String serviceTitleSuffix, String purposeMarkdown) {
        return new Info()
                .title("LogiTrack — " + serviceTitleSuffix)
                .description(purposeMarkdown)
                .version(VERSION)
                .contact(contact());
    }

    public static Server deployedServer(String publicBaseUrl) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String url =
                    publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
            return new Server()
                    .url(url)
                    .description(
                            "URL base configurada con `openapi.server.url` (útil si publicas el JSON fuera del mismo origen).");
        }
        return new Server()
                .url("/")
                .description(
                        "Mismo origen que esta aplicación: el host y puerto con el que accedes en local, staging o producción. "
                                + "«Try it out» en Swagger usa ese origen automáticamente.");
    }
}
