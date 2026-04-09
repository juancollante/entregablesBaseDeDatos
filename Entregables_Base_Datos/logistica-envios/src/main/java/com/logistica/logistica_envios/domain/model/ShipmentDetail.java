package com.logistica.logistica_envios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentDetail(
        UUID id,
        String numeroGuia,
        String estadoCodigo,
        String estadoNombre,
        Instant fechaCreacion,
        LocalDate fechaEstimadaEntrega,
        String codigoSedeRegistro,
        UUID creadoPorUsuarioId,
        String descripcionPaquete,
        BigDecimal pesoKg,
        PartyContact remitente,
        PartyContact destinatario
) {
}
