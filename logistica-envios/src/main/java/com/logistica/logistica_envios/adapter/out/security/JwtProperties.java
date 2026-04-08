package com.logistica.logistica_envios.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "";

    public String getSecret() {
        if (secret == null || secret.isBlank()) {
            return "dev-jwt-secret-change-in-production-min-32-chars!!";
        }
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
