package com.logistica.logistica_seguimiento.adapter.out.persistence.repository;

import com.logistica.logistica_seguimiento.adapter.out.persistence.entity.TipoEventoSeguimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoEventoSeguimientoJpaRepository extends JpaRepository<TipoEventoSeguimientoEntity, UUID> {

    Optional<TipoEventoSeguimientoEntity> findByCodigoIgnoreCase(String codigo);
}

