package com.logistica.logistica_notificaciones.adapter.out.persistence.repository;

import com.logistica.logistica_notificaciones.adapter.out.persistence.entity.ConsumoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConsumoEventoJpaRepository extends JpaRepository<ConsumoEventoEntity, UUID> {

    Optional<ConsumoEventoEntity> findByIdempotencyKey(String idempotencyKey);
}

