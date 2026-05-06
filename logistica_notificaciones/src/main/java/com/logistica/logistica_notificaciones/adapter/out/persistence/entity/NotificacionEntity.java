package com.logistica.logistica_notificaciones.adapter.out.persistence.entity;

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
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
public class NotificacionEntity {

    public NotificacionEntity() {
    }

    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String canal;

    @Column(nullable = false, length = 255)
    private String destinatario;

    @Column(length = 300)
    private String asunto;

    @Column(columnDefinition = "text")
    private String cuerpo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plantilla_id")
    private PlantillaNotificacionEntity plantilla;

    @Column(nullable = false, length = 40)
    private String estado;

    @Column(name = "numero_guia", length = 60)
    private String numeroGuia;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_evento", columnDefinition = "jsonb")
    private String payloadEventoJson;

    @Column(name = "proveedor_id_mensaje", length = 255)
    private String proveedorIdMensaje;

    @Column(name = "error_mensaje", length = 1000)
    private String errorMensaje;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public PlantillaNotificacionEntity getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(PlantillaNotificacionEntity plantilla) {
        this.plantilla = plantilla;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getPayloadEventoJson() {
        return payloadEventoJson;
    }

    public void setPayloadEventoJson(String payloadEventoJson) {
        this.payloadEventoJson = payloadEventoJson;
    }

    public String getProveedorIdMensaje() {
        return proveedorIdMensaje;
    }

    public void setProveedorIdMensaje(String proveedorIdMensaje) {
        this.proveedorIdMensaje = proveedorIdMensaje;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }

    public void setErrorMensaje(String errorMensaje) {
        this.errorMensaje = errorMensaje;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}

