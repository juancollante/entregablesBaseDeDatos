package com.logistica.logistica_envios.adapter.in.web.dto;

import com.logistica.logistica_envios.domain.model.ShipmentDetail;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Vista pública de un envío: sin datos personales ni direcciones completas.
 */
public record ShipmentPublicResponse(
        String numeroGuia,
        String estadoCodigo,
        String estadoNombre,
        Instant fechaCreacion,
        LocalDate fechaEstimadaEntrega,
        String municipioOrigenNombre,
        String municipioDestinoNombre
) {
    public static ShipmentPublicResponse from(ShipmentDetail d) {
        String origen = d.remitente().direccion().municipioNombre();
        String destino = d.destinatario().direccion().municipioNombre();
        return new ShipmentPublicResponse(
                d.numeroGuia(),
                d.estadoCodigo(),
                d.estadoNombre(),
                d.fechaCreacion(),
                d.fechaEstimadaEntrega(),
                origen,
                destino
        );
    }
}
