package com.logistica.logistica_seguimiento.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TrackingEvent(
        UUID id,
        String numeroGuia,
        String tipoEventoCodigo,
        String sedeCodigo,
        Instant fechaHora,
        String descripcion,
        Map<String, Object> metadata,
        UUID operadorIdExterno
) {
}

