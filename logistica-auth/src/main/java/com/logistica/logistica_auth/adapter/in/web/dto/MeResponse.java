package com.logistica.logistica_auth.adapter.in.web.dto;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        String role,
        String codigoSedeAsignada
) {
}
