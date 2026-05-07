package com.logistica.logistica_seguimiento.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Textos y esquema JWT alineados con el resto de microservicios LogiTrack (OpenAPI 3).
 */
public final class OpenApiDocSupport {

    public static final String VERSION = "0.1";
    public static final String JWT_SCHEME = "bearerAuth";

    private static final String JWT_HELP =
            "\n\n**JWT:** en Swagger UI use **Authorize** y pegue solo el *access token* (sin el prefijo `Bearer `). "
                    + "Los tokens los emiten `POST /api/auth/login` y `POST /api/auth/register` en **logistica-auth**.";

    private OpenApiDocSupport() {}

    public static Contact contact() {
        return new Contact().name("LogiTrack (EAV06)");
    }

    public static Info info(String serviceTitleSuffix, String purposeMarkdown, boolean includeJwtHelp) {
        String desc = purposeMarkdown + (includeJwtHelp ? JWT_HELP : "");
        return new Info()
                .title("LogiTrack — " + serviceTitleSuffix)
                .description(desc)
                .version(VERSION)
                .contact(contact());
    }

    public static SecurityScheme bearerJwt() {
        return new SecurityScheme()
                .name(JWT_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access token JWT. Cabecera: Authorization: Bearer {token}");
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
