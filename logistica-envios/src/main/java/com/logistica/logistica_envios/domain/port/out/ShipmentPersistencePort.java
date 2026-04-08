package com.logistica.logistica_envios.domain.port.out;

import com.logistica.logistica_envios.domain.model.NewShipment;
import com.logistica.logistica_envios.domain.model.ShipmentDetail;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentPersistencePort {

    ShipmentDetail saveNew(NewShipment shipment, UUID creadoPorUsuarioId, String codigoSedeRegistro);

    Optional<ShipmentDetail> findByNumeroGuia(String numeroGuia);
}
