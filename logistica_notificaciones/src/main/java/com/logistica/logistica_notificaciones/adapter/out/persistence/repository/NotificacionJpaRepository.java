package com.logistica.logistica_notificaciones.adapter.out.persistence.repository;

import com.logistica.logistica_notificaciones.adapter.out.persistence.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, UUID> {
}

