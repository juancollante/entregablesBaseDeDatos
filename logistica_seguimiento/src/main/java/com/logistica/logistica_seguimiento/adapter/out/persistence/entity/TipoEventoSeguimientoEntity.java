package com.logistica.logistica_seguimiento.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tipos_evento_seguimiento")
public class TipoEventoSeguimientoEntity {

    public TipoEventoSeguimientoEntity() {
    }

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

