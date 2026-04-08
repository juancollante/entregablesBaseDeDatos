package com.logistica.logistica_envios.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "auditoria_envios")
public class AuditoriaEnvioEntity {

    public AuditoriaEnvioEntity() {
    }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false)
    private EnvioEntity envio;

    @Column(nullable = false, length = 80)
    private String accion;

    @Column(name = "usuario_id_externo")
    private UUID usuarioIdExterno;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_anteriores", columnDefinition = "jsonb")
    private Map<String, Object> valoresAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_nuevos", columnDefinition = "jsonb")
    private Map<String, Object> valoresNuevos;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EnvioEntity getEnvio() {
        return envio;
    }

    public void setEnvio(EnvioEntity envio) {
        this.envio = envio;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public UUID getUsuarioIdExterno() {
        return usuarioIdExterno;
    }

    public void setUsuarioIdExterno(UUID usuarioIdExterno) {
        this.usuarioIdExterno = usuarioIdExterno;
    }

    public Map<String, Object> getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(Map<String, Object> valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public Map<String, Object> getValoresNuevos() {
        return valoresNuevos;
    }

    public void setValoresNuevos(Map<String, Object> valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
