package com.logistica.logistica_envios.adapter.out.persistence.repository;

import com.logistica.logistica_envios.adapter.out.persistence.entity.EnvioEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EnvioJpaRepository extends JpaRepository<EnvioEntity, UUID> {

    boolean existsByNumeroGuia(String numeroGuia);

    @EntityGraph(attributePaths = {
            "estadoEnvio",
            "remitente",
            "remitente.direccion",
            "destinatario",
            "destinatario.direccion"
    })
    @Query("SELECT e FROM EnvioEntity e WHERE e.numeroGuia = :guia")
    Optional<EnvioEntity> findDetailedByNumeroGuia(@Param("guia") String guia);
}
