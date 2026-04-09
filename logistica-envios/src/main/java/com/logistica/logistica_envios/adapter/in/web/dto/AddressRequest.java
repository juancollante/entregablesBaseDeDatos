package com.logistica.logistica_envios.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String linea1,
        String linea2,
        String municipioCodigoDane,
        String municipioNombre,
        String codigoPostal,
        String referencias
) {
}
