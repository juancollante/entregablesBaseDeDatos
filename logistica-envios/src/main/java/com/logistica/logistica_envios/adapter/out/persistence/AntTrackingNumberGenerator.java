package com.logistica.logistica_envios.adapter.out.persistence;

import com.logistica.logistica_envios.domain.port.out.TrackingNumberPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AntTrackingNumberGenerator implements TrackingNumberPort {

    private static final String PREFIX = "ANT-";

    @Override
    public String nextNumeroGuia() {
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return PREFIX + hex;
    }
}
