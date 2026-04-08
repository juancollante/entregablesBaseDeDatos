package com.logistica.logistica_envios.adapter.in.web.dto;

public record AddressResponse(
        String linea1,
        String linea2,
        String municipioCodigoDane,
        String municipioNombre,
        String departamento,
        String pais,
        String codigoPostal,
        String referencias
) {
}
