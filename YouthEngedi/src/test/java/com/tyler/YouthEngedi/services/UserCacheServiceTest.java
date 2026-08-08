package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.InvalidEmailException;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.UserCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCacheServiceTest class Unit Tests")
class UserCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String,Object> valueOperations;

    @InjectMocks
    private UserCacheService userCacheService;

    private UserCache cache;
    private User user;
    private String testValue;

    @BeforeEach
    void setUp(){
        String userKey = "user:email";
        user = User.builder().id(12L).email("test123@gmail.com").build();
        cache = UserCache.builder().id(user.getId()).email(user.getEmail()).build();
        testValue = userKey + ":test123@gmail.com";

        ReflectionTestUtils.setField(userCacheService,"redisTTL",30);
    }

    @Test
    @DisplayName("Should get user from cache")
    void shouldGetUserFromCache(){
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(testValue)).thenReturn(cache);

        UserCache actual = userCacheService.get(user.getEmail());

        assertEquals(cache,actual);

        verify(valueOperations).get(testValue);
    }

    @Test
    @DisplayName("Should put user into cache")
    void shouldPutUserIntoCache(){
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        userCacheService.put(user.getEmail(),cache);

        verify(valueOperations).set(eq(testValue),eq(cache),eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Should evict user from cache")
    void shouldEvictUserFromCache(){

        userCacheService.evict(cache.getEmail());

        verify(redisTemplate).delete(testValue);
    }

    @Test
    @DisplayName("Should return null When User not found in cache")
    void shouldReturnNullWhenUserNotFoundInCache(){

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(testValue)).thenReturn(null);

        UserCache actual = userCacheService.get(user.getEmail());

        assertNull(actual);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(testValue);
    }
}