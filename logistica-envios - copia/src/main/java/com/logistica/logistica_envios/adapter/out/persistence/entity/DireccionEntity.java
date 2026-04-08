package com.logistica.logistica_envios.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "direcciones")
public class DireccionEntity {

    public DireccionEntity() {
    }

    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String linea1;

    @Column(length = 255)
    private String linea2;

    @Column(name = "municipio_codigo_dane", length = 5)
    private String municipioCodigoDane;

    @Column(name = "municipio_nombre", length = 120)
    private String municipioNombre;

    @Column(nullable = false, length = 80)
    private String departamento = "Antioquia";

    @Column(nullable = false, length = 80)
    private String pais = "Colombia";

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(length = 500)
    private String referencias;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLinea1() {
        return linea1;
    }

    public void setLinea1(String linea1) {
        this.linea1 = linea1;
    }

    public String getLinea2() {
        return linea2;
    }

    public void setLinea2(String linea2) {
        this.linea2 = linea2;
    }

    public String getMunicipioCodigoDane() {
        return municipioCodigoDane;
    }

    public void setMunicipioCodigoDane(String municipioCodigoDane) {
        this.municipioCodigoDane = municipioCodigoDane;
    }

    public String getMunicipioNombre() {
        return municipioNombre;
    }

    public void setMunicipioNombre(String municipioNombre) {
        this.municipioNombre = municipioNombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }
}
