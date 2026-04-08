package com.logistica.logistica_auth.domain.model;

import java.util.UUID;

public record UserAccount(
        UUID id,
        String email,
        String passwordHash,
        String nombre,
        String apellido,
        String roleCode,
        String codigoSedeAsignada,
        boolean activo
) {
}
