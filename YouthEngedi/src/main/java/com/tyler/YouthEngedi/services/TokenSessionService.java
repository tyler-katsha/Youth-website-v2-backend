package com.tyler.YouthEngedi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class TokenSessionService {

    private final static Logger logger = LoggerFactory.getLogger(TokenSessionService.class);
    private final static Duration REFRESH_TTL = Duration.ofDays(7);


    private final RedisTemplate<String,String> redisTemplate;
    private final RedisScript<Object> rotateRefreshTokenScript;

    public TokenSessionService(RedisTemplate<String,String> template){
        this.redisTemplate = template;
        this.rotateRefreshTokenScript = RedisScript.of(new ClassPathResource("scripts/rotate-refresh-token.lua"), Object.class);
    }

    private String buildKey(long userId,String familyId){
        return "auth:refresh:" + userId + ":" + familyId;
    }

    /**
     * Initializes a new token family on primary login
     * */
    public String createTokenFamily(long userId,String familyId){
        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(buildKey(userId,familyId),refreshToken,REFRESH_TTL);
        return userId + "." + familyId + "." + refreshToken;
    }

    /**
     * Rotates the token if valid, Revokes the whole family if a reuse attempt is detected
     * */
    public String rotateToken(long userId,String familyId,String presentedToken){
        var key = buildKey(userId,familyId);

        var newSecret = UUID.randomUUID().toString();

        var ttlSeconds = REFRESH_TTL.getSeconds();

        Object result = redisTemplate.execute(rotateRefreshTokenScript, Collections.singletonList(key),presentedToken,newSecret,String.valueOf(ttlSeconds));


        if(result instanceof Long status){

            // Scenario 1: Session expired or already logged out
            if(status == 0){
                throw new SecurityException("Session expired or invalid. Please re-authenticate.");
            }

            // Scenario 2: Token Reuse / Breach Detected!
            // The user presented a valid UUID, but it's not the active one for this family
            if(status == -1){
                redisTemplate.delete(key); // Family Revocation: Kill all sessions for this family
                logger.warn("ALERT: Refresh token reuse detected for user {}! Session family {} revoked.", userId, familyId);
                throw new SecurityException("Security breach detected. All active sessions invalidated.");
            }

        }

        if(result == null){
            throw new SecurityException("Session expired or invalid. Please re-authenticate.");
        }

        return result.toString();
    }

    /**
     * Explicit logout: delete the token from redis
     * */
    public void revokeSession(long userId,String familyId){
        redisTemplate.delete(buildKey(userId, familyId));
    }
}
