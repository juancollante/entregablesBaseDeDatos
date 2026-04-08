package com.logistica.logistica_envios.domain.model;

public record PostalAddress(
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
