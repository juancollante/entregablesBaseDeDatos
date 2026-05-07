package com.logistica.logistica_seguimiento.application.service;

import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;
import com.logistica.logistica_seguimiento.domain.port.in.GetTrackingHistoryUseCase;
import com.logistica.logistica_seguimiento.domain.port.in.RegisterTrackingEventUseCase;
import com.logistica.logistica_seguimiento.domain.port.out.TrackingEventPersistencePort;
import com.logistica.logistica_seguimiento.domain.port.out.TrackingEventPublishedPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrackingApplicationService implements RegisterTrackingEventUseCase, GetTrackingHistoryUseCase {

    private final TrackingEventPersistencePort persistencePort;
    private final TrackingEventPublishedPort publishedPort;

    public TrackingApplicationService(
            TrackingEventPersistencePort persistencePort,
            TrackingEventPublishedPort publishedPort
    ) {
        this.persistencePort = persistencePort;
        this.publishedPort = publishedPort;
    }

    @Override
    @Transactional
    public TrackingEvent register(
            String numeroGuia,
            String tipoEventoCodigo,
            String sedeCodigo,
            Instant fechaHora,
            String descripcion,
            Map<String, Object> metadata,
            UUID operadorIdExterno
    ) {
        if (numeroGuia == null || numeroGuia.isBlank()) {
            throw new IllegalArgumentException("numeroGuia es obligatorio");
        }
        if (tipoEventoCodigo == null || tipoEventoCodigo.isBlank()) {
            throw new IllegalArgumentException("tipoEventoCodigo es obligatorio");
        }
        if (sedeCodigo == null || sedeCodigo.isBlank()) {
            throw new IllegalArgumentException("sedeCodigo es obligatorio");
        }
        if (persistencePort.findSedeIdByCodigo(sedeCodigo.trim()).isEmpty()) {
            throw new IllegalArgumentException("Sede no existe: " + sedeCodigo);
        }
        if (persistencePort.findTipoEventoIdByCodigo(tipoEventoCodigo.trim()).isEmpty()) {
            throw new IllegalArgumentException("Tipo de evento no existe: " + tipoEventoCodigo);
        }

        TrackingEvent saved = persistencePort.save(new TrackingEvent(
                UUID.randomUUID(),
                numeroGuia.trim(),
                tipoEventoCodigo.trim().toUpperCase(),
                sedeCodigo.trim().toUpperCase(),
                fechaHora == null ? Instant.now() : fechaHora,
                descripcion,
                metadata == null ? Map.of() : metadata,
                operadorIdExterno
        ));

        publishedPort.onTrackingEventRegistered(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEvent> getHistory(String numeroGuia) {
        if (numeroGuia == null || numeroGuia.isBlank()) {
            throw new IllegalArgumentException("numeroGuia es obligatorio");
        }
        return persistencePort.findByNumeroGuiaOrdered(numeroGuia.trim());
    }
}

