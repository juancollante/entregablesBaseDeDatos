package com.logistica.logistica_envios.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartyRequest(
        @NotBlank String nombreCompleto,
        String documento,
        String email,
        String telefono,
        @NotNull @Valid AddressRequest direccion
) {
}
