package com.logistica.logistica_envios.domain.port.in;

import com.logistica.logistica_envios.domain.model.NewShipment;
import com.logistica.logistica_envios.domain.model.ShipmentDetail;

import java.util.UUID;

public interface RegisterShipmentUseCase {

    ShipmentDetail register(NewShipment shipment, UUID operadorUsuarioId, String codigoSedeDesdeToken);
}
