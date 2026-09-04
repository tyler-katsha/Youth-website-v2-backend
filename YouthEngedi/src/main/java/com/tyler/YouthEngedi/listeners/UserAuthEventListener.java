package com.tyler.YouthEngedi.listeners;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import com.tyler.YouthEngedi.models.events.*;
import com.tyler.YouthEngedi.services.DashboardHistoryService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserAuthEventListener {

    private final SimpMessagingTemplate messageTemplate;
    private final DashboardHistoryService historyService;

    private final static String ADMIN_EVENTS_TOPIC = "/admin/events";

    public UserAuthEventListener(SimpMessagingTemplate messageTemplate, DashboardHistoryService historyService){
        this.messageTemplate = messageTemplate;
        this.historyService = historyService;
    }

    @Async
    @EventListener
    public void handleLogin(UserLoginEvent event){
        processAndBroadcast(event,ConnectionType.CONNECT);
    }

    @Async
    @EventListener
    public void handleLogout(UserLogoutEvent event){
        processAndBroadcast(event,ConnectionType.DISCONNECT);
    }

    @Async
    @EventListener
    public void handleContinueAsGuest(ContinueAsGuestEvent event){
        processAndBroadcast(event,ConnectionType.CONTINUE_AS_GUEST);
    }

    private void processAndBroadcast(BaseAuthEvent event,ConnectionType type){
        var socketEvent = WebSocketEvent.builder()
                .connectionType(type)
                .email(event.getEmail())
                .message(event.getMessage())
                .userId(event.getUserId())
                .timeStamp(event.getTimeStamp())
                .build();

        historyService.recordBuffer(socketEvent);

        var payload = Traffic.builder()
                .webSocketEvent(socketEvent)
                .size(historyService.getConnectCountWithGuest())
                .maxEvents(historyService.getMaxEvents())
                .build();

        messageTemplate.convertAndSend(ADMIN_EVENTS_TOPIC,payload);
    }
}
