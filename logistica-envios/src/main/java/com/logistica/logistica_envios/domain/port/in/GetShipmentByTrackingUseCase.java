package com.logistica.logistica_envios.domain.port.in;

import com.logistica.logistica_envios.domain.model.ShipmentDetail;

public interface GetShipmentByTrackingUseCase {

    ShipmentDetail getByNumeroGuia(String numeroGuia);
}
