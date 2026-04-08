package com.logistica.logistica_auth.adapter.out.security;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public final class JwtSigningKey {

    private static final String DEV_FALLBACK =
            "dev-jwt-secret-change-in-production-min-32-chars!!";

    private JwtSigningKey() {
    }

    public static SecretKey fromConfig(String secret) {
        String trimmed = secret == null ? "" : secret.trim();
        if (trimmed.isEmpty()) {
            trimmed = DEV_FALLBACK;
        }
        byte[] keyMaterial = toKeyMaterial(trimmed);
        return Keys.hmacShaKeyFor(keyMaterial);
    }

    /**
     * HS256 requiere al menos 256 bits (32 bytes). Texto corto o Base64 corto se deriva con SHA-256.
     */
    private static byte[] toKeyMaterial(String trimmed) {
        try {
            byte[] decoded = Decoders.BASE64.decode(trimmed);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // no es Base64 válido; usar texto plano
        }
        byte[] raw = trimmed.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) {
            return raw;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
