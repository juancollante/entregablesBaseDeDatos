package com.logistica.logistica_seguimiento.domain.port.in;

import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface RegisterTrackingEventUseCase {

    TrackingEvent register(
            String numeroGuia,
            String tipoEventoCodigo,
            String sedeCodigo,
            Instant fechaHora,
            String descripcion,
            Map<String, Object> metadata,
            UUID operadorIdExterno
    );
}

