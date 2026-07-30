package com.tyler.YouthEngedi.sockets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;

@Component
public class WebSocketEventListener {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String ONLINE_USERS = "online:users";

    @EventListener
    public void handleWebSocketConnectListeners(SessionConnectedEvent event){
        String userId = WebSocketUtils.getUserId(event);

        redisTemplate.opsForSet().add(ONLINE_USERS, userId);
        redisTemplate.opsForValue().set("heartbeat:" + userId, String.valueOf(Instant.now().toEpochMilli()));

        System.out.println("User ONLINE: " + userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListeners(SessionDisconnectEvent event){
        String userId = WebSocketUtils.getUserId(event);

        redisTemplate.opsForSet().remove(ONLINE_USERS, userId);
        redisTemplate.delete("heartbeat:" + userId);

        System.out.println("User OFFLINE: " + userId);
    }
}
