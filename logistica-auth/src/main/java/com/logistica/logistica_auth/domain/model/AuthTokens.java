package com.logistica.logistica_auth.domain.model;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds
) {
}
