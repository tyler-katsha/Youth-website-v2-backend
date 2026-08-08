package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.ActiveUserRepository;
import com.tyler.YouthEngedi.models.ActiveUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActiveUserServiceTest class Unit Tests")
class ActiveUserServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String,Object> valueOperations;
    @Mock
    private SetOperations<String,Object> setOperations;
    @Mock
    private ActiveUserRepository activeUserRepository;

    @InjectMocks
    private ActiveUserService activeUserService;

    private Month month;
    private String redisKey;
    @BeforeEach
    void setUp(){
        String ACTIVE_USERS_KEY = "active_users:";
        redisKey = ACTIVE_USERS_KEY + LocalDate.now();
        month = LocalDate.now().getMonth();
    }

    @Nested
    @DisplayName("Add records unit testing")
    class PersistRecords{

        @Test
        @DisplayName("Should create a new monthly record")
        void shouldCreateNewRecord(){

            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.size(redisKey)).thenReturn(7L);
            when(activeUserRepository.findById(month)).thenReturn(Optional.empty());

            activeUserService.persistDailyActiveUsers();

            ArgumentCaptor<ActiveUser> captor = ArgumentCaptor.forClass(ActiveUser.class);

            verify(activeUserRepository).save(captor.capture());

            ActiveUser saved = captor.getValue();

            assertEquals(month,saved.getMonth());
            assertEquals(7,saved.getActiveTotal());

            verify(redisTemplate).delete(redisKey);
        }
        @Test
        @DisplayName("Should update existing record")
        void shouldUpdateExistingRecord(){

            ActiveUser existing = ActiveUser.builder()
                    .month(month)
                    .activeTotal(49)
                    .build();

            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.size(redisKey)).thenReturn(5L);
            when(activeUserRepository.findById(month)).thenReturn(Optional.of(existing));

            activeUserService.persistDailyActiveUsers();

            assertEquals(54,existing.getActiveTotal());

            verify(setOperations).size(redisKey);
            verify(activeUserRepository).findById(month);
            verify(activeUserRepository).save(existing);
            verify(redisTemplate).delete(redisKey);
        }

        @Test
        @DisplayName("Should persist zero active users")
        void shouldPersistZeroActiveUsers(){

            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.size(redisKey)).thenReturn(0L);
            when(activeUserRepository.findById(month)).thenReturn(Optional.empty());

            activeUserService.persistDailyActiveUsers();

            ArgumentCaptor<ActiveUser> captor = ArgumentCaptor.forClass(ActiveUser.class);

            verify(activeUserRepository).save(captor.capture());

            ActiveUser saved = captor.getValue();

            assertEquals(month,saved.getMonth());
            assertEquals(0,saved.getActiveTotal());

            verify(redisTemplate).delete(redisKey);
        }
    }

    @Test
    @DisplayName("Increase active user count by one")
    void increaseActiveUserCountByOne(){

    }

    @Test
    @DisplayName("Decrease active user count by one")
    void decreaseActiveUserCountByOne(){

    }

    @Test
    @DisplayName("Get active user count based on month")
    void getActiveUserCountBasedOnMonth(){

    }
}