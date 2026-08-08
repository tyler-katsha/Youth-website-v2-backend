package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.InvalidEmailException;
import com.tyler.YouthEngedi.models.dtos.UserCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.redis.expiration-minutes}")
    private int redisTTL;

    private String key(String email) {
        return "user:email:" + email;
    }

    public UserCache get(String email) {
        return (UserCache) redisTemplate.opsForValue().get(key(email));
    }

    public void put(String email, UserCache dto) {
        redisTemplate.opsForValue().set(key(email), dto, Duration.ofMinutes(redisTTL));
    }

    public void evict(String email) {
        redisTemplate.delete(key(email));
    }
}
