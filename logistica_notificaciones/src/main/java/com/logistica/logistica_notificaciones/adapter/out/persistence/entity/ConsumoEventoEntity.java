package com.logistica.logistica_notificaciones.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consumo_eventos")
public class ConsumoEventoEntity {

    public ConsumoEventoEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 200)
    private String idempotencyKey;

    @Column(length = 200)
    private String topic;

    @Column(name = "partition_id")
    private Integer partitionId;

    @Column(name = "offset_val")
    private Long offsetVal;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public Long getOffsetVal() {
        return offsetVal;
    }

    public void setOffsetVal(Long offsetVal) {
        this.offsetVal = offsetVal;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(Instant procesadoEn) {
        this.procesadoEn = procesadoEn;
    }
}

