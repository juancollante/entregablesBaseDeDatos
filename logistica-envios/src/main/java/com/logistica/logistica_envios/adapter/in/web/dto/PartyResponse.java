package com.logistica.logistica_envios.adapter.in.web.dto;

public record PartyResponse(
        String nombreCompleto,
        String documento,
        String email,
        String telefono,
        AddressResponse direccion
) {
}
