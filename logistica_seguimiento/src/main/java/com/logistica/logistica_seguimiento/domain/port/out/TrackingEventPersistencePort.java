package com.logistica.logistica_seguimiento.domain.port.out;

import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingEventPersistencePort {

    TrackingEvent save(TrackingEvent event);

    List<TrackingEvent> findByNumeroGuiaOrdered(String numeroGuia);

    Optional<UUID> findSedeIdByCodigo(String sedeCodigo);

    Optional<UUID> findTipoEventoIdByCodigo(String tipoEventoCodigo);
}

