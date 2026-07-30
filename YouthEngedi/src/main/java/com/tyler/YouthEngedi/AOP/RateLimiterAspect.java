package com.tyler.YouthEngedi.AOP;

import com.tyler.YouthEngedi.Exceptions.RateLimitExceededException;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimiterAspect {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Around("annotation(rateLimited")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = request.getRemoteAddr();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        String prefix = rateLimited.key().isEmpty() ? methodName:rateLimited.key();
        String bucketKey = prefix + "-" + ipAddress;

        Bucket bucket = rateLimiterService.resolveBucket(bucketKey,rateLimited);

        if(bucket.tryConsume(1)){
            return joinPoint.proceed();
        } else{
            throw new RateLimitExceededException("Rate limit exceeded for endpoint: " + methodName);
        }
    }
}
