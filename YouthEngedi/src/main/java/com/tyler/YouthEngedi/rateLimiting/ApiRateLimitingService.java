package com.tyler.YouthEngedi.rateLimiting;

import com.tyler.YouthEngedi.annotations.RateLimited;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiRateLimitingService {

    // Key: composite string (e.g. "192.168.1.1#UserController.getProfile")
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public ConsumptionProbe consume(String key, RateLimited config){
        Bucket bucket = buckets.computeIfAbsent(key,k -> createNewBucket(config));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createNewBucket(RateLimited config){

        Duration duration = Duration.of(config.duration(),config.unit());

        Bandwidth limit = Bandwidth.builder()
                .capacity(config.capacity())
                .refillGreedy(config.tokens(), duration)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
