package com.logistica.logistica_envios.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "remitentes")
public class RemitenteEntity {

    public RemitenteEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(length = 50)
    private String documento;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direccion_id", nullable = false)
    private DireccionEntity direccion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public DireccionEntity getDireccion() {
        return direccion;
    }

    public void setDireccion(DireccionEntity direccion) {
        this.direccion = direccion;
    }
}
