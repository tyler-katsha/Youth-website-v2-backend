package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import com.tyler.YouthEngedi.models.events.WebSocketEvent;
import com.tyler.YouthEngedi.utils.GuestManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class DashboardHistoryService {

    private static final int MAX_EVENTS = 50;

    private final ConcurrentLinkedDeque<WebSocketEvent> eventBuffer = new ConcurrentLinkedDeque<>();

    private final Set<Long> activeOnlineUsers = ConcurrentHashMap.newKeySet();

    public synchronized void recordBuffer(WebSocketEvent event) {
        if (event == null) {
            return;
        }

        eventBuffer.addLast(event);

        while (eventBuffer.size() > MAX_EVENTS) {
            eventBuffer.pollFirst();
        }

        var userId = event.getUserId();

        if (userId != null && userId > 0) {
            if (ConnectionType.CONNECT.equals(event.getConnectionType())) {
                activeOnlineUsers.add(userId);
            } else if (ConnectionType.DISCONNECT.equals(event.getConnectionType())) {
                activeOnlineUsers.remove(userId);
            }
        }
    }

    /**
     * Returns a snapshot of the most recent events (oldest to newest).
     */
    public List<WebSocketEvent> getRecentEvents() {
        return new ArrayList<>(eventBuffer);
    }

    /**
     * Accurately returns the count of unique registered users currently online.
     */
    public long getRegisteredUserCount() {
        return activeOnlineUsers.size();
    }

    /**
     * Returns registered users + active guests currently connected.
     */
    public long getConnectCountWithGuest() {
        return getRegisteredUserCount() + GuestManager.getActiveGuestCount();
    }

    /**
     * Direct query to check if a specific user is currently connected.
     */
    public boolean isUserConnected(Long userId) {
        return userId != null && activeOnlineUsers.contains(userId);
    }

    public int getMaxEvents() {
        return MAX_EVENTS;
    }
}