package com.logistica.logistica_auth.adapter.out.persistence.repository;

import com.logistica.logistica_auth.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByEmailIgnoreCaseAndActivoIsTrue(String email);
}
