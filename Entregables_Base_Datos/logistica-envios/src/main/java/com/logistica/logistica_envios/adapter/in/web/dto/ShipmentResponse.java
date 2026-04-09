package com.logistica.logistica_envios.adapter.in.web.dto;

import com.logistica.logistica_envios.domain.model.PartyContact;
import com.logistica.logistica_envios.domain.model.PostalAddress;
import com.logistica.logistica_envios.domain.model.ShipmentDetail;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        String numeroGuia,
        String estadoCodigo,
        String estadoNombre,
        Instant fechaCreacion,
        LocalDate fechaEstimadaEntrega,
        String codigoSedeRegistro,
        UUID creadoPorUsuarioId,
        String descripcionPaquete,
        BigDecimal pesoKg,
        PartyResponse remitente,
        PartyResponse destinatario
) {
    public static ShipmentResponse from(ShipmentDetail d) {
        return new ShipmentResponse(
                d.id(),
                d.numeroGuia(),
                d.estadoCodigo(),
                d.estadoNombre(),
                d.fechaCreacion(),
                d.fechaEstimadaEntrega(),
                d.codigoSedeRegistro(),
                d.creadoPorUsuarioId(),
                d.descripcionPaquete(),
                d.pesoKg(),
                toParty(d.remitente()),
                toParty(d.destinatario())
        );
    }

    private static PartyResponse toParty(PartyContact p) {
        return new PartyResponse(
                p.nombreCompleto(),
                p.documento(),
                p.email(),
                p.telefono(),
                toAddress(p.direccion())
        );
    }

    private static AddressResponse toAddress(PostalAddress a) {
        return new AddressResponse(
                a.linea1(),
                a.linea2(),
                a.municipioCodigoDane(),
                a.municipioNombre(),
                a.departamento(),
                a.pais(),
                a.codigoPostal(),
                a.referencias()
        );
    }
}
