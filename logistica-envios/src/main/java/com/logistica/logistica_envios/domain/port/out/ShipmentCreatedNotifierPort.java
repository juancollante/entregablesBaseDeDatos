package com.logistica.logistica_envios.domain.port.out;

import com.logistica.logistica_envios.domain.model.ShipmentDetail;

public interface ShipmentCreatedNotifierPort {

    void onShipmentCreated(ShipmentDetail detail);
}
