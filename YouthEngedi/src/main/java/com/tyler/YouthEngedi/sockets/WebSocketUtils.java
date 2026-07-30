package com.tyler.YouthEngedi.sockets;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class WebSocketUtils {
    public static String getUserId(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        return (String) accessor.getSessionAttributes().get("userId");
    }

    public static String getUserId(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        return (String) accessor.getSessionAttributes().get("userId");
    }
}
