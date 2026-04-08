package com.logistica.logistica_envios.adapter.out.persistence.repository;

import com.logistica.logistica_envios.adapter.out.persistence.entity.DestinatarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DestinatarioJpaRepository extends JpaRepository<DestinatarioEntity, UUID> {
}
