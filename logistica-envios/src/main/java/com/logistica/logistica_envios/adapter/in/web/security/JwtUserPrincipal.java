package com.logistica.logistica_envios.adapter.in.web.security;

import java.util.UUID;

public record JwtUserPrincipal(
        UUID id,
        String email,
        String roleCode,
        String codigoSedeAsignada
) {
}
