package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.ActiveUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ActiveUserRepository extends JpaRepository<ActiveUser, LocalDate> {
}
