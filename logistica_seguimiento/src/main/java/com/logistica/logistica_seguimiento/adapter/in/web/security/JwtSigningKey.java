package com.logistica.logistica_seguimiento.adapter.in.web.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class JwtSigningKey {

    private JwtSigningKey() {
    }

    public static SecretKey fromConfig(String secret) {
        String trimmed = secret == null ? "" : secret.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalStateException("JWT secret vacío: define APP_JWT_SECRET");
        }
        return Keys.hmacShaKeyFor(toKeyMaterial(trimmed));
    }

    private static byte[] toKeyMaterial(String trimmed) {
        try {
            byte[] decoded = Decoders.BASE64.decode(trimmed);
            if (decoded.length >= 32) return decoded;
        } catch (IllegalArgumentException ignored) {
            // no base64
        }
        byte[] raw = trimmed.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 32) return raw;
        try {
            return MessageDigest.getInstance("SHA-256").digest(raw);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

