package com.logistica.logistica_envios.adapter.out.persistence.repository;

import com.logistica.logistica_envios.adapter.out.persistence.entity.EstadoEnvioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EstadoEnvioJpaRepository extends JpaRepository<EstadoEnvioEntity, UUID> {

    Optional<EstadoEnvioEntity> findByCodigo(String codigo);
}
