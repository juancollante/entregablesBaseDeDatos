package com.logistica.logistica_envios.domain.exception;

public class InvalidShipmentException extends RuntimeException {

    public InvalidShipmentException(String message) {
        super(message);
    }
}
