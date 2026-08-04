package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.ActiveUserRepository;
import com.tyler.YouthEngedi.models.ActiveUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActiveUserService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ActiveUserRepository activeUserRepository;

    public static final String ACTIVE_USERS_KEY = "active:users:";

    public void persistDailyActiveUsers() {
        LocalDate month = LocalDate.now().withDayOfMonth(1);

        String redisKey = "active:users:" + LocalDate.now();

        Long activeCount = redisTemplate.opsForSet().size(redisKey);

        ActiveUser record = activeUserRepository.findById(month)
                .orElse(ActiveUser.builder()
                        .month(month)
                        .activeTotal(0)
                        .build());

        record.setActiveTotal(record.getActiveTotal() + (activeCount != null ? activeCount : 0));

        activeUserRepository.save(record);

        redisTemplate.delete(redisKey);
    }

    public void incrementActiveUserCount(){
        redisTemplate.opsForValue().increment(ACTIVE_USERS_KEY);
    }
    public void decrementActiveUserCount(){
        redisTemplate.opsForValue().decrement(ACTIVE_USERS_KEY);
    }
    public long getActiveUserCount() {
        Object value = redisTemplate.opsForValue().get(ACTIVE_USERS_KEY);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
