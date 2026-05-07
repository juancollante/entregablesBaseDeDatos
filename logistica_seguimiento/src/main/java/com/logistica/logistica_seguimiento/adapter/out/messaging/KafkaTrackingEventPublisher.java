package com.logistica.logistica_seguimiento.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;
import com.logistica.logistica_seguimiento.domain.port.out.TrackingEventPublishedPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaTrackingEventPublisher implements TrackingEventPublishedPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaTrackingEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaTrackingEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic:logistica.eventbus}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void onTrackingEventRegistered(TrackingEvent event) {
        try {
            String id = UUID.randomUUID().toString();
            Map<String, Object> payload = Map.of(
                    "numeroGuia", event.numeroGuia(),
                    "tipoEventoCodigo", event.tipoEventoCodigo(),
                    "sedeCodigo", event.sedeCodigo(),
                    "fechaHora", event.fechaHora().toString(),
                    "operadorIdExterno", event.operadorIdExterno() == null ? "" : event.operadorIdExterno().toString()
            );
            Map<String, Object> evt = Map.of(
                    "id", id,
                    "type", "tracking.event.registered",
                    "occurredAt", Instant.now().toString(),
                    "payload", payload
            );
            kafkaTemplate.send(topic, event.numeroGuia(), objectMapper.writeValueAsString(evt));
        } catch (Exception ex) {
            log.warn("No se pudo publicar evento tracking.event.registered a Kafka", ex);
        }
    }
}

