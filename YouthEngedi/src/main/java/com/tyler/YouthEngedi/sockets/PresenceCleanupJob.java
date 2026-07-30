package com.tyler.YouthEngedi.sockets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PresenceCleanupJob {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String ONLINE_USERS = "online:users";
    private static final long TIMEOUT = 60000; // 60s

    @Scheduled(fixedRate = 30000)
    public void cleanup() {
        Set<String> users = redisTemplate.opsForSet().members(ONLINE_USERS);

        if (users == null) return;

        long now = System.currentTimeMillis();

        for (String userId : users) {
            String lastSeenStr = redisTemplate.opsForValue().get("heartbeat:" + userId);

            if (lastSeenStr == null) {
                forceOffline(userId);
                continue;
            }

            long lastSeen = Long.parseLong(lastSeenStr);

            if (now - lastSeen > TIMEOUT) {
                forceOffline(userId);
            }
        }
    }

    private void forceOffline(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS, userId);
        redisTemplate.delete("heartbeat:" + userId);

        // System.out.println("FORCED OFFLINE (timeout): " + userId);
    }
}
