package com.logistica.logistica_envios.adapter.out.messaging;

import com.logistica.logistica_envios.domain.model.ShipmentDetail;
import com.logistica.logistica_envios.domain.port.out.ShipmentCreatedNotifierPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingShipmentCreatedNotifier implements ShipmentCreatedNotifierPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingShipmentCreatedNotifier.class);

    @Override
    public void onShipmentCreated(ShipmentDetail detail) {
        log.info("Envío creado: guia={} id={} (evento listo para publicar a Kafka en una fase posterior)", detail.numeroGuia(), detail.id());
    }
}
