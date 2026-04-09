package com.logistica.logistica_envios.adapter.out.persistence.repository;

import com.logistica.logistica_envios.adapter.out.persistence.entity.DireccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DireccionJpaRepository extends JpaRepository<DireccionEntity, UUID> {
}
