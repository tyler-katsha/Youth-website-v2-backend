package com.tyler.YouthEngedi.sockets;

import com.tyler.YouthEngedi.models.AdminEvent;
import com.tyler.YouthEngedi.models.enums.ConnectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
public class PresenceListener {

    @Autowired
    private AdminEventPublisher publisher;

    @EventListener
    public void onConnect(SessionConnectedEvent event){

        String userId = WebSocketUtils.getUserId(event);

        publisher.publish(AdminEvent
                .builder()
                .type(ConnectionType.CONNECT)
                .userId(userId)
                .message("User Connected")
                .timestamp(System.currentTimeMillis())
                .build());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event){

        String userId = WebSocketUtils.getUserId(event);

        publisher.publish(AdminEvent
                .builder()
                .type(ConnectionType.DISCONNECT)
                .userId(userId)
                .message("User Disconnected")
                .timestamp(System.currentTimeMillis())
                .build());
    }
}
