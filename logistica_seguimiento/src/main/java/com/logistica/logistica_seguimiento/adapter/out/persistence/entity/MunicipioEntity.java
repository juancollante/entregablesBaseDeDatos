package com.logistica.logistica_seguimiento.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "municipios")
public class MunicipioEntity {

    public MunicipioEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "codigo_dane", nullable = false, unique = true, length = 5)
    private String codigoDane;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigoDane() {
        return codigoDane;
    }

    public void setCodigoDane(String codigoDane) {
        this.codigoDane = codigoDane;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
