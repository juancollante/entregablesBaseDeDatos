package com.logistica.logistica_seguimiento.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistica.logistica_seguimiento.adapter.out.persistence.entity.EventoSeguimientoEntity;
import com.logistica.logistica_seguimiento.adapter.out.persistence.repository.EventoSeguimientoJpaRepository;
import com.logistica.logistica_seguimiento.adapter.out.persistence.repository.SedeJpaRepository;
import com.logistica.logistica_seguimiento.adapter.out.persistence.repository.TipoEventoSeguimientoJpaRepository;
import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;
import com.logistica.logistica_seguimiento.domain.port.out.TrackingEventPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TrackingPersistenceAdapter implements TrackingEventPersistencePort {

    private final EventoSeguimientoJpaRepository eventoRepo;
    private final SedeJpaRepository sedeRepo;
    private final TipoEventoSeguimientoJpaRepository tipoRepo;
    private final ObjectMapper objectMapper;

    public TrackingPersistenceAdapter(
            EventoSeguimientoJpaRepository eventoRepo,
            SedeJpaRepository sedeRepo,
            TipoEventoSeguimientoJpaRepository tipoRepo,
            ObjectMapper objectMapper
    ) {
        this.eventoRepo = eventoRepo;
        this.sedeRepo = sedeRepo;
        this.tipoRepo = tipoRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public TrackingEvent save(TrackingEvent event) {
        UUID tipoId = tipoRepo.findByCodigoIgnoreCase(event.tipoEventoCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de evento no existe: " + event.tipoEventoCodigo()))
                .getId();
        UUID sedeId = sedeRepo.findByCodigoIgnoreCase(event.sedeCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Sede no existe: " + event.sedeCodigo()))
                .getId();

        EventoSeguimientoEntity e = new EventoSeguimientoEntity();
        e.setId(event.id());
        e.setNumeroGuia(event.numeroGuia());
        e.setTipoEventoId(tipoId);
        e.setSedeId(sedeId);
        e.setFechaHora(event.fechaHora());
        e.setDescripcion(event.descripcion());
        try {
            e.setMetadata(objectMapper.writeValueAsString(event.metadata()));
        } catch (Exception ex) {
            e.setMetadata("{}");
        }
        e.setOperadorIdExterno(event.operadorIdExterno());
        e.setCreatedAt(Instant.now());

        eventoRepo.save(e);
        return event;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEvent> findByNumeroGuiaOrdered(String numeroGuia) {
        return eventoRepo.findByNumeroGuiaOrdered(numeroGuia).stream().map(e -> new TrackingEvent(
                e.getId(),
                e.getNumeroGuia(),
                tipoRepo.findById(e.getTipoEventoId()).map(t -> t.getCodigo()).orElse(""),
                sedeRepo.findById(e.getSedeId()).map(s -> s.getCodigo()).orElse(""),
                e.getFechaHora(),
                e.getDescripcion(),
                readMetadata(e.getMetadata()),
                e.getOperadorIdExterno()
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findSedeIdByCodigo(String sedeCodigo) {
        return sedeRepo.findByCodigoIgnoreCase(sedeCodigo).map(s -> s.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findTipoEventoIdByCodigo(String tipoEventoCodigo) {
        return tipoRepo.findByCodigoIgnoreCase(tipoEventoCodigo).map(t -> t.getId());
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> readMetadata(String json) {
        if (json == null || json.isBlank()) return java.util.Map.of();
        try {
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception ignored) {
            return java.util.Map.of();
        }
    }
}

