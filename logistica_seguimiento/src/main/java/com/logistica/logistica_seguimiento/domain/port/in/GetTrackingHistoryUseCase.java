package com.logistica.logistica_seguimiento.domain.port.in;

import com.logistica.logistica_seguimiento.domain.model.TrackingEvent;

import java.util.List;

public interface GetTrackingHistoryUseCase {

    List<TrackingEvent> getHistory(String numeroGuia);
}

