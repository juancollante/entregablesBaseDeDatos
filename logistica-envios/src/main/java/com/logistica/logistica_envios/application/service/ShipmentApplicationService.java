package com.logistica.logistica_envios.application.service;

import com.logistica.logistica_envios.domain.exception.InvalidShipmentException;
import com.logistica.logistica_envios.domain.exception.ShipmentNotFoundException;
import com.logistica.logistica_envios.domain.model.NewShipment;
import com.logistica.logistica_envios.domain.model.PostalAddress;
import com.logistica.logistica_envios.domain.model.ShipmentDetail;
import com.logistica.logistica_envios.domain.port.in.GetShipmentByTrackingUseCase;
import com.logistica.logistica_envios.domain.port.in.RegisterShipmentUseCase;
import com.logistica.logistica_envios.domain.port.out.ShipmentCreatedNotifierPort;
import com.logistica.logistica_envios.domain.port.out.ShipmentPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShipmentApplicationService implements RegisterShipmentUseCase, GetShipmentByTrackingUseCase {

    private final ShipmentPersistencePort shipmentPersistencePort;
    private final ShipmentCreatedNotifierPort shipmentCreatedNotifierPort;

    public ShipmentApplicationService(
            ShipmentPersistencePort shipmentPersistencePort,
            ShipmentCreatedNotifierPort shipmentCreatedNotifierPort
    ) {
        this.shipmentPersistencePort = shipmentPersistencePort;
        this.shipmentCreatedNotifierPort = shipmentCreatedNotifierPort;
    }

    @Override
    @Transactional
    public ShipmentDetail register(NewShipment shipment, UUID operadorUsuarioId, String codigoSedeDesdeToken) {
        validateAddresses(shipment.remitente().direccion());
        validateAddresses(shipment.destinatario().direccion());
        String sede = resolveCodigoSede(shipment.codigoSedeRegistroOverride(), codigoSedeDesdeToken);
        ShipmentDetail saved = shipmentPersistencePort.saveNew(shipment, operadorUsuarioId, sede);
        shipmentCreatedNotifierPort.onShipmentCreated(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDetail getByNumeroGuia(String numeroGuia) {
        return shipmentPersistencePort.findByNumeroGuia(numeroGuia.trim())
                .orElseThrow(() -> new ShipmentNotFoundException(numeroGuia));
    }

    private static void validateAddresses(PostalAddress d) {
        String dane = d.municipioCodigoDane();
        if (dane != null && !dane.isBlank() && dane.length() != 5) {
            throw new InvalidShipmentException("municipioCodigoDane debe tener 5 dígitos (DANE)");
        }
    }

    private static String resolveCodigoSede(String override, String desdeToken) {
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        if (desdeToken != null && !desdeToken.isBlank()) {
            return desdeToken.trim();
        }
        return null;
    }
}
