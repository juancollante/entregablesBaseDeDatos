package com.logistica.logistica_auth.adapter.in.web.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public static TokenResponse from(String access, String refresh, long expiresInSeconds) {
        return new TokenResponse(access, refresh, "Bearer", expiresInSeconds);
    }
}
