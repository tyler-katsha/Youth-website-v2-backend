package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import com.tyler.YouthEngedi.models.events.EventKey;
import com.tyler.YouthEngedi.models.events.WebSocketEvent;
import com.tyler.YouthEngedi.utils.GuestManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class DashboardHistoryService {

    private static final int MAX_EVENTS = 50;

    private final ConcurrentLinkedDeque<WebSocketEvent> eventBuffer = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<EventKey, WebSocketEvent> eventIndex = new ConcurrentHashMap<>();

    public synchronized void recordBuffer(WebSocketEvent event) {
        var key = EventKey.builder()
                .eventUserId(event.getUserId())
                .connectionType(event.getConnectionType())
                .build();

        var existingEvent = eventIndex.putIfAbsent(key, event);

        if (existingEvent == null) {
            eventBuffer.addLast(event);
        }

        while (eventBuffer.size() > MAX_EVENTS) {
            var removed = eventBuffer.pollFirst();

            if (removed != null) {
                var removedKey = EventKey.builder()
                        .eventUserId(removed.getUserId())
                        .connectionType(removed.getConnectionType())
                        .build();

                eventIndex.remove(removedKey, removed);
            }
        }
    }

    public List<WebSocketEvent> getRecentEvents() {
        return new ArrayList<>(eventBuffer);
    }

    public long getRegisteredUserCount() {
        return eventBuffer.stream()
                .filter(event -> ConnectionType.CONNECT.equals(event.getConnectionType()))
                .filter(event -> event.getUserId() != null && event.getUserId() > 0)
                .count();
    }
    public long getConnectCountWithGuest(){
        return getRegisteredUserCount() + GuestManager.getActiveGuestCount();
    }

    public int getMaxEvents() {
        return MAX_EVENTS;
    }
}