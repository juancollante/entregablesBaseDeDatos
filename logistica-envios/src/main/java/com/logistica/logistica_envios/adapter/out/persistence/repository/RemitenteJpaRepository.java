package com.logistica.logistica_envios.adapter.out.persistence.repository;

import com.logistica.logistica_envios.adapter.out.persistence.entity.RemitenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RemitenteJpaRepository extends JpaRepository<RemitenteEntity, UUID> {
}
