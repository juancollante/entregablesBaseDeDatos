package com.logistica.logistica_seguimiento.adapter.out.persistence.repository;

import com.logistica.logistica_seguimiento.adapter.out.persistence.entity.EventoSeguimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventoSeguimientoJpaRepository extends JpaRepository<EventoSeguimientoEntity, UUID> {

    @Query("select e from EventoSeguimientoEntity e where e.numeroGuia = :numeroGuia order by e.fechaHora asc")
    List<EventoSeguimientoEntity> findByNumeroGuiaOrdered(@Param("numeroGuia") String numeroGuia);
}

