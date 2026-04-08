package com.logistica.logistica_envios.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "envios")
public class EnvioEntity {

    public EnvioEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "numero_guia", nullable = false, unique = true, length = 32)
    private String numeroGuia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_envio_id", nullable = false)
    private EstadoEnvioEntity estadoEnvio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private RemitenteEntity remitente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private DestinatarioEntity destinatario;

    @Column(name = "descripcion_paquete", length = 500)
    private String descripcionPaquete;

    @Column(name = "peso_kg", precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_estimada_entrega")
    private LocalDate fechaEstimadaEntrega;

    @Column(name = "codigo_sede_registro", length = 50)
    private String codigoSedeRegistro;

    @Column(name = "creado_por_usuario_id_externo")
    private UUID creadoPorUsuarioIdExterno;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public EstadoEnvioEntity getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(EstadoEnvioEntity estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public RemitenteEntity getRemitente() {
        return remitente;
    }

    public void setRemitente(RemitenteEntity remitente) {
        this.remitente = remitente;
    }

    public DestinatarioEntity getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(DestinatarioEntity destinatario) {
        this.destinatario = destinatario;
    }

    public String getDescripcionPaquete() {
        return descripcionPaquete;
    }

    public void setDescripcionPaquete(String descripcionPaquete) {
        this.descripcionPaquete = descripcionPaquete;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    public void setFechaEstimadaEntrega(LocalDate fechaEstimadaEntrega) {
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }

    public String getCodigoSedeRegistro() {
        return codigoSedeRegistro;
    }

    public void setCodigoSedeRegistro(String codigoSedeRegistro) {
        this.codigoSedeRegistro = codigoSedeRegistro;
    }

    public UUID getCreadoPorUsuarioIdExterno() {
        return creadoPorUsuarioIdExterno;
    }

    public void setCreadoPorUsuarioIdExterno(UUID creadoPorUsuarioIdExterno) {
        this.creadoPorUsuarioIdExterno = creadoPorUsuarioIdExterno;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
