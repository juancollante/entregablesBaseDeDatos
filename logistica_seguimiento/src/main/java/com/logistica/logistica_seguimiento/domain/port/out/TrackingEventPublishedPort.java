package com.logistica.logistica_seguimiento.domain.port.out;

import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;

public interface TrackingEventPublishedPort {

    void onTrackingEventRegistered(TrackingEvent event);
}

