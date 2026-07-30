package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.annotations.RateLimited;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, RateLimited annotation){
        return buckets.computeIfAbsent(key,k -> createNewBucket(annotation));
    }

    private Bucket createNewBucket(RateLimited annotation){
        Duration duration = parseDuration(annotation.refillDuration());
        Refill refill = Refill.intervally(annotation.refillTokens(),duration);
        Bandwidth limit = Bandwidth.classic(annotation.capacity(),refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Duration parseDuration(String durationStr) {
        if (durationStr.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(durationStr.replace("s", "")));
        } else if (durationStr.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(durationStr.replace("m", "")));
        } else if (durationStr.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(durationStr.replace("h", "")));
        } else {
            throw new IllegalArgumentException("Invalid duration format: " + durationStr + ". Use 's', 'm', or 'h'.");
        }
    }

}
