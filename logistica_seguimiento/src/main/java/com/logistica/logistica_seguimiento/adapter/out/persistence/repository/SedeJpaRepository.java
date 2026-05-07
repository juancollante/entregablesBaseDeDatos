package com.logistica.logistica_seguimiento.adapter.out.persistence.repository;

import com.logistica.logistica_seguimiento.adapter.out.persistence.entity.SedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SedeJpaRepository extends JpaRepository<SedeEntity, UUID> {

    Optional<SedeEntity> findByCodigoIgnoreCase(String codigo);

    @Query(
            "select distinct s from SedeEntity s join fetch s.municipio m "
                    + "where s.activo = true and m.activo = true order by m.nombre asc, s.codigo asc"
    )
    List<SedeEntity> findAllActivasWithMunicipioOrdenadas();
}

