package com.tyler.YouthEngedi.rateLimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FingerprintRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(50)
                .refillGreedy(10, Duration.ofSeconds(1))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    public boolean isAllowed(String ja3Fingerprint){
        Bucket bucket = buckets.computeIfAbsent(ja3Fingerprint,k -> createBucket());
        return bucket.tryConsume(1);
    }

}
