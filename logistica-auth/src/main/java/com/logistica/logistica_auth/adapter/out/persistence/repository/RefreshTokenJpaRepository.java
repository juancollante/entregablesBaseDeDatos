package com.logistica.logistica_auth.adapter.out.persistence.repository;

import com.logistica.logistica_auth.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {
}
