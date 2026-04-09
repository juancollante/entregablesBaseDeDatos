package com.logistica.logistica_envios.domain.exception;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(String numeroGuia) {
        super("Envío no encontrado: " + numeroGuia);
    }
}
