package com.logistica.logistica_notificaciones.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistica.logistica_notificaciones.adapter.out.persistence.entity.ConsumoEventoEntity;
import com.logistica.logistica_notificaciones.adapter.out.persistence.entity.NotificacionEntity;
import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.PlantillaNotificacionJpaRepository;
import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.ConsumoEventoJpaRepository;
import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.NotificacionJpaRepository;
import com.logistica.logistica_notificaciones.domain.model.EventBusMessage;
import com.logistica.logistica_notificaciones.service.NotificationPushService;
import com.logistica.logistica_notificaciones.service.OutboundEmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private static final String EVENT_SHIPMENT_CREATED = "shipment.created";
    private static final String EVENT_TRACKING_REGISTERED = "tracking.event.registered";
    private static final String EVENT_AUTH_LOGIN = "auth.login";

    private final ObjectMapper objectMapper;
    private final ConsumoEventoJpaRepository consumoEventoJpaRepository;
    private final NotificacionJpaRepository notificacionJpaRepository;
    private final PlantillaNotificacionJpaRepository plantillaNotificacionJpaRepository;
    private final NotificationPushService notificationPushService;
    private final OutboundEmailService outboundEmailService;

    public KafkaEventConsumer(
            ObjectMapper objectMapper,
            ConsumoEventoJpaRepository consumoEventoJpaRepository,
            NotificacionJpaRepository notificacionJpaRepository,
            PlantillaNotificacionJpaRepository plantillaNotificacionJpaRepository,
            NotificationPushService notificationPushService,
            OutboundEmailService outboundEmailService
    ) {
        this.objectMapper = objectMapper;
        this.consumoEventoJpaRepository = consumoEventoJpaRepository;
        this.notificacionJpaRepository = notificacionJpaRepository;
        this.plantillaNotificacionJpaRepository = plantillaNotificacionJpaRepository;
        this.notificationPushService = notificationPushService;
        this.outboundEmailService = outboundEmailService;
    }

    @KafkaListener(topics = "${app.kafka.topic}")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        EventBusMessage msg;
        try {
            msg = objectMapper.readValue(consumerRecord.value(), EventBusMessage.class);
        } catch (Exception parseError) {
            log.warn("Evento inválido (JSON). topic={} partition={} offset={}", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
            return;
        }
        String idemKey = msg.id();

        if (idemKey == null || idemKey.isBlank()) {
            log.warn("Evento sin id (se ignora). topic={} partition={} offset={}", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
            return;
        }
        // Idempotencia: primero intentamos registrar el consumo. Si colisiona por unique, ignoramos.
        try {
            ConsumoEventoEntity consumption = new ConsumoEventoEntity();
            consumption.setId(UUID.randomUUID());
            consumption.setIdempotencyKey(idemKey);
            consumption.setTopic(consumerRecord.topic());
            consumption.setPartitionId(consumerRecord.partition());
            consumption.setOffsetVal(consumerRecord.offset());
            consumption.setProcesadoEn(Instant.now());
            consumoEventoJpaRepository.save(consumption);
        } catch (DataIntegrityViolationException alreadyProcessed) {
            return;
        }

        NotificacionEntity notif = toNotification(msg);
        notificacionJpaRepository.save(notif);

        // Push en tiempo real (WebSocket) al UI
        notificationPushService.broadcast(toPushPayload(msg, notif));

        // Envío de email (si está configurado). En MVP: solo cuando exista destinatarioEmail.
        outboundEmailService.trySendEmail(notif.getDestinatario(), notif.getAsunto(), notif.getCuerpo());

        log.info("Notificación creada. eventType={} notifId={} destinatario={}", msg.type(), notif.getId(), notif.getDestinatario());
    }

    private NotificacionEntity toNotification(EventBusMessage msg) {
        Map<String, Object> payload = msg.payload() == null ? Map.of() : msg.payload();
        String email = string(payload.get("destinatarioEmail"));
        String guia = string(payload.get("numeroGuia"));
        String templateCode = templateCodeFor(msg.type());
        var template = plantillaNotificacionJpaRepository.findByCodigoIgnoreCaseAndActivoIsTrue(templateCode).orElse(null);
        String asunto = template == null ? buildSubject(msg.type(), guia) : template.getAsunto();
        String cuerpo = template == null ? buildBody(msg.type(), payload) : template.getCuerpo();

        NotificacionEntity n = new NotificacionEntity();
        n.setId(UUID.randomUUID());
        n.setCanal("PUSH");
        n.setDestinatario(email == null || email.isBlank() ? "anon" : email.trim().toLowerCase());
        n.setAsunto(asunto);
        n.setCuerpo(cuerpo);
        n.setPlantilla(template);
        n.setEstado("PENDIENTE");
        n.setNumeroGuia(guia);
        try {
            n.setPayloadEventoJson(objectMapper.writeValueAsString(msg));
        } catch (Exception ignored) {
            n.setPayloadEventoJson("{}");
        }
        n.setCreatedAt(Instant.now());
        return n;
    }

    private static String templateCodeFor(String type) {
        return switch (type == null ? "" : type) {
            case EVENT_SHIPMENT_CREATED -> "SHIPMENT_CREATED";
            case EVENT_TRACKING_REGISTERED -> "TRACKING_EVENT_REGISTERED";
            case EVENT_AUTH_LOGIN -> "AUTH_LOGIN";
            default -> "GENERICA";
        };
    }

    private static Map<String, Object> toPushPayload(EventBusMessage msg, NotificacionEntity notif) {
        return Map.of(
                "notificationId", notif.getId().toString(),
                "eventId", msg.id(),
                "type", msg.type(),
                "occurredAt", msg.occurredAt() == null ? Instant.now().toString() : msg.occurredAt().toString(),
                "numeroGuia", notif.getNumeroGuia(),
                "title", notif.getAsunto(),
                "message", notif.getCuerpo()
        );
    }

    private static String buildSubject(String type, String numeroGuia) {
        String g = numeroGuia == null ? "" : (" (" + numeroGuia + ")");
        return switch (type == null ? "" : type) {
            case EVENT_SHIPMENT_CREATED -> "Envío creado" + g;
            case EVENT_TRACKING_REGISTERED -> "Actualización de seguimiento" + g;
            case EVENT_AUTH_LOGIN -> "Inicio de sesión" + g;
            default -> "Notificación" + g;
        };
    }

    private static String buildBody(String type, Map<String, Object> payload) {
        if (EVENT_SHIPMENT_CREATED.equals(type)) {
            return "Se registró un envío en el sistema.";
        }
        if (EVENT_TRACKING_REGISTERED.equals(type)) {
            Object tipo = payload.get("tipoEventoCodigo");
            Object sede = payload.get("sedeCodigo");
            return "Nuevo evento de seguimiento: " + (tipo == null ? "N/A" : tipo) + " en " + (sede == null ? "N/A" : sede);
        }
        if (EVENT_AUTH_LOGIN.equals(type)) {
            Object st = payload.get("statusLogin");
            return "Login: " + (st == null ? "N/A" : st);
        }
        return "Evento recibido: " + type;
    }

    private static String string(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}

