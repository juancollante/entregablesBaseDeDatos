package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.domain.port.out.AccessTokenTtlPort;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenTtlAdapter implements AccessTokenTtlPort {

    private final long expiresInSeconds;

    public JwtAccessTokenTtlAdapter(JwtProperties jwtProperties) {
        this.expiresInSeconds = jwtProperties.getAccessTokenMinutes() * 60L;
    }

    @Override
    public long expiresInSeconds() {
        return expiresInSeconds;
    }
}
