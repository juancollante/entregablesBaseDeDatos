package com.logistica.logistica_envios.domain.model;

public record PartyContact(
        String nombreCompleto,
        String documento,
        String email,
        String telefono,
        PostalAddress direccion
) {
}
