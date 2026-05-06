package com.logistica.logistica_notificaciones.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationPushService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationPushService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void broadcast(Map<String, Object> payload) {
        simpMessagingTemplate.convertAndSend("/topic/notificaciones", payload);
    }
}

