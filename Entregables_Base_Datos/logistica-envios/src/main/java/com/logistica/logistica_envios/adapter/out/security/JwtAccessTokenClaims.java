package com.logistica.logistica_envios.adapter.out.security;

/**
 * Debe coincidir con los claims emitidos por logistica-auth ({@code JwtAccessTokenAdapter}).
 */
public final class JwtAccessTokenClaims {

    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SEDE = "sede";

    private JwtAccessTokenClaims() {
    }
}
