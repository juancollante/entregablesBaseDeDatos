package com.logistica.logistica_notificaciones.adapter.out.persistence.repository;

import com.logistica.logistica_notificaciones.adapter.out.persistence.entity.PlantillaNotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlantillaNotificacionJpaRepository extends JpaRepository<PlantillaNotificacionEntity, UUID> {

    Optional<PlantillaNotificacionEntity> findByCodigoIgnoreCaseAndActivoIsTrue(String codigo);
}

