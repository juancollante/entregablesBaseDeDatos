package com.logistica.logistica_envios.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NewShipment(
        PartyContact remitente,
        PartyContact destinatario,
        String descripcionPaquete,
        BigDecimal pesoKg,
        LocalDate fechaEstimadaEntrega,
        String codigoSedeRegistroOverride
) {
}
