package com.tyler.YouthEngedi.sockets;

import com.tyler.YouthEngedi.models.AdminEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
public class AdminEventPublisher {

    @Autowired
    private SimpMessagingTemplate simpleMessageTemplate;

    private static final String ADMIN_TOPIC = "/admin/events";

    public void publish(AdminEvent event){
        simpleMessageTemplate.convertAndSend(ADMIN_TOPIC,event);
    }
}
