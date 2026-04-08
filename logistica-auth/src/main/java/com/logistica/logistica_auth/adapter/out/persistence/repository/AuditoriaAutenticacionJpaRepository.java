package com.logistica.logistica_auth.adapter.out.persistence.repository;

import com.logistica.logistica_auth.adapter.out.persistence.entity.AuditoriaAutenticacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaAutenticacionJpaRepository extends JpaRepository<AuditoriaAutenticacionEntity, UUID> {
}
