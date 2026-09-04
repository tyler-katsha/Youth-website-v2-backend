package com.tyler.YouthEngedi.Aspect;

import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.rateLimiting.ApiRateLimitingService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimitingAspect {

    private final ApiRateLimitingService rateLimitingService;

    public RateLimitingAspect(ApiRateLimitingService rateLimitingService){
        this.rateLimitingService = rateLimitingService;
    }

    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(attributes == null){
            return joinPoint.proceed();
        }

        var request = attributes.getRequest();
        var response = attributes.getResponse();

        var clientIdentifier = getClientIdentifier(request);
        var methodSignature = joinPoint.getSignature().toShortString();
        var composeKey = clientIdentifier + ":" + methodSignature;

        ConsumptionProbe probe = rateLimitingService.consume(composeKey,rateLimited);

        if(!probe.isConsumed()){
            long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);

            if(response != null){
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setHeader("X-Rate-Limit-Remaining","0");

                response.setContentType("application/json");

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

                response.getWriter().write("""
                        {
                            "status": 429,
                            "error": "Too Many Request",
                            "message": "Rate Limit exceeded. Try again in %d seconds"
                        }
                        """.formatted(retryAfterSeconds));
            }
            return null;
        }

        if(response != null){
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        }

        return joinPoint.proceed();
    }

    private String getClientIdentifier(HttpServletRequest request){
        String apiKey = request.getHeader("X-API-KEY");

        if(apiKey != null && !apiKey.isBlank()){
            return "apiKey:" + apiKey;
        }

        String xForwardFor = request.getHeader("X-Forwarded-For");
        if(xForwardFor != null && !xForwardFor.isBlank()){
            return "ip:" + xForwardFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }
}
