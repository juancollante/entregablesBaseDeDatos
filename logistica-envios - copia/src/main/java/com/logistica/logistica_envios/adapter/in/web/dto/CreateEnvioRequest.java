package com.logistica.logistica_envios.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEnvioRequest(
        @NotNull @Valid PartyRequest remitente,
        @NotNull @Valid PartyRequest destinatario,
        String descripcionPaquete,
        BigDecimal pesoKg,
        LocalDate fechaEstimadaEntrega,
        String codigoSedeRegistro
) {
}
