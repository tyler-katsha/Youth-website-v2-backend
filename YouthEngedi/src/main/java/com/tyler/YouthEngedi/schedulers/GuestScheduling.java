package com.tyler.YouthEngedi.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.tyler.YouthEngedi.utils.GuestManager.guests;
import static com.tyler.YouthEngedi.utils.IdManager.releaseId;

@Component
public final class GuestScheduling {

    private static final long GUEST_TTL_MILLIS = 30 * 60 * 1000L; // 30 minutes

    @Scheduled(fixedDelay = 60_000)
    public void evictExpiredGuest(){
        long now = Instant.now().toEpochMilli();

        guests().entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getCreatedAt() > GUEST_TTL_MILLIS);

            if(expired){
                releaseId(entry.getKey());
            }

            return expired;
        });
    }
}
