package com.logistica.logistica_auth.domain.port.out;

/**
 * Vigencia del access token en segundos (configuración aportada por el adaptador de infraestructura).
 */
public interface AccessTokenTtlPort {

    long expiresInSeconds();
}
