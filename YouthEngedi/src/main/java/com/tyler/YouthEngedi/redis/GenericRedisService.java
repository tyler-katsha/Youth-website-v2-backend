package com.tyler.YouthEngedi.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GenericRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public GenericRedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public <T> Optional<T> get(String key, Class<T> targetClass) {
        Object val = redisTemplate.opsForValue().get(key);

        if (val == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.convertValue(val, targetClass));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public <T> Optional<List<T>> getListValue(String key, Class<T> elementClass) {
        Object val = redisTemplate.opsForValue().get(key);

        if (val == null) {
            return Optional.empty();
        }

        // If RedisTemplate already deserialized it directly to a List
        if (val instanceof List<?> list) {
            // Safe check or mapping if needed
            try {
                JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
                List<T> targetList = objectMapper.convertValue(list, type);
                return Optional.ofNullable(targetList);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
            List<T> list = objectMapper.convertValue(val, type);
            return Optional.ofNullable(list);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}