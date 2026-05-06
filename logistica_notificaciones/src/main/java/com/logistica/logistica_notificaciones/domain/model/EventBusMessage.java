package com.logistica.logistica_notificaciones.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato simple de evento en el Event Bus (Kafka).
 * Lo mantenemos agnóstico al microservicio emisor y fácil de extender.
 */
public record EventBusMessage(
        String id,
        String type,
        Instant occurredAt,
        Map<String, Object> payload
) {
}

