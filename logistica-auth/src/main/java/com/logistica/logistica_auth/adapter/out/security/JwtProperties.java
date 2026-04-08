package com.logistica.logistica_auth.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "";
    private int accessTokenMinutes = 15;
    private int refreshTokenDays = 7;

    public String getSecret() {
        if (secret == null || secret.isBlank()) {
            return "dev-jwt-secret-change-in-production-min-32-chars!!";
        }
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(int accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public int getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(int refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }
}
