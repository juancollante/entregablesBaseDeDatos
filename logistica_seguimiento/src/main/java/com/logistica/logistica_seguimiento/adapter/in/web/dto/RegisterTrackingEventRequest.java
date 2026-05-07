package com.logistica.logistica_seguimiento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

public record RegisterTrackingEventRequest(
        @NotBlank(message = "numeroGuia es obligatorio")
        String numeroGuia,
        @NotBlank(message = "tipoEventoCodigo es obligatorio")
        String tipoEventoCodigo,
        @NotBlank(message = "sedeCodigo es obligatorio")
        String sedeCodigo,
        Instant fechaHora,
        String descripcion,
        Map<String, Object> metadata
) {
}

