package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.AuditRepository;
import com.tyler.YouthEngedi.Repository.PerformanceRepository;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.Performance;
import com.tyler.YouthEngedi.models.dtos.CachedPageResponse;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AdminService {

    private final AuditRepository auditRepository;
    private final PerformanceRepository performanceRepository;
    private final GenericRedisService redisService;

    private static final String ADMIN_LOGS_PAGE_KEY_PREFIX = "logs:page:";
    private static final String ADMIN_PERFORMANCE_PAGE_KEY_PREFIX = "performances:page:";
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

    public AdminService(AuditRepository auditRepository,PerformanceRepository performanceRepository,GenericRedisService redisService){
        this.auditRepository = auditRepository;
        this.performanceRepository = performanceRepository;
        this.redisService = redisService;
    }
    public Page<AuditLog> getSystemLogs(int page, int size){
        var cacheKey = ADMIN_LOGS_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var logsPage = auditRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"now")));

        var responseToCache = CachedPageResponse.of(logsPage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return logsPage;
    }

    public Page<Performance> getSystemPerformanceData(int page, int size){
        var cacheKey = ADMIN_PERFORMANCE_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var performancePage = performanceRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt")));

        var responseToCache = CachedPageResponse.of(performancePage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return performancePage;
    }
}
