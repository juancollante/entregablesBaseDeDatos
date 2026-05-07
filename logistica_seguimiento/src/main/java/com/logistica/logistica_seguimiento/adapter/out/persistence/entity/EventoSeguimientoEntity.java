package com.logistica.logistica_seguimiento.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_seguimiento")
public class EventoSeguimientoEntity {

    public EventoSeguimientoEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "numero_guia", nullable = false, length = 60)
    private String numeroGuia;

    @Column(name = "tipo_evento_id", nullable = false)
    private UUID tipoEventoId;

    @Column(name = "sede_id")
    private UUID sedeId;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(length = 500)
    private String descripcion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "operador_id_externo")
    private UUID operadorIdExterno;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public UUID getTipoEventoId() {
        return tipoEventoId;
    }

    public void setTipoEventoId(UUID tipoEventoId) {
        this.tipoEventoId = tipoEventoId;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public UUID getOperadorIdExterno() {
        return operadorIdExterno;
    }

    public void setOperadorIdExterno(UUID operadorIdExterno) {
        this.operadorIdExterno = operadorIdExterno;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

