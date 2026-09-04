package com.tyler.YouthEngedi.rateLimiting;

import com.tyler.YouthEngedi.annotations.RateLimited;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiRateLimitingServiceTest class Unit Tests")
class ApiRateLimitingServiceTest {

    private ApiRateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new ApiRateLimitingService();
    }

    @Test
    @DisplayName("Should allow requests up to capacity")
    void shouldAllowRequestsUpToCapacity() throws Exception {

        var config = getRateLimitedConfig();

        var key = "ip:192.168.1.1:testMethod";

        for (int i = 0; i < 5; i++) {

            var probe = rateLimitingService.consume(key, config);

            assertTrue(probe.isConsumed(), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject request when bucket is exhausted")
    void shouldRejectRequestWhenBucketIsExhausted() throws Exception {

        var config = getRateLimitedConfig();

        var key = "ip:192.168.1.1:testMethod";

        // Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            var probe = rateLimitingService.consume(key, config);

            assertTrue(probe.isConsumed());
        }

        // 6th request should fail
        var probe = rateLimitingService.consume(key, config);

        assertFalse(probe.isConsumed());

        assertTrue(probe.getNanosToWaitForRefill() > 0, "Should provide a refill wait time");
    }

    @Test
    @DisplayName("Different keys should have independent buckets")
    void differentKeysShouldHaveIndependentBuckets() throws Exception {

        var config = getRateLimitedConfig();

        var key1 = "ip:192.168.1.1:testMethod";
        var key2 = "ip:192.168.1.2:testMethod";

        // Exhaust key1
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitingService.consume(key1, config).isConsumed());
        }

        // key1 is blocked
        assertFalse(rateLimitingService.consume(key1, config).isConsumed());

        // key2 still has its own bucket
        assertTrue(rateLimitingService.consume(key2, config).isConsumed());
    }

    @Test
    @DisplayName("Should return correct remaining token count")
    void shouldReturnCorrectRemainingTokens() throws Exception {

        RateLimited config = getRateLimitedConfig();

        var key = "ip:192.168.1.1:testMethod";

        ConsumptionProbe probe =
                rateLimitingService.consume(key, config);

        assertTrue(probe.isConsumed());
        assertEquals(4, probe.getRemainingTokens());

        probe = rateLimitingService.consume(key, config);

        assertEquals(3, probe.getRemainingTokens());

        probe = rateLimitingService.consume(key, config);

        assertEquals(2, probe.getRemainingTokens());

        probe = rateLimitingService.consume(key, config);

        assertEquals(1, probe.getRemainingTokens());

        probe = rateLimitingService.consume(key, config);

        assertEquals(0, probe.getRemainingTokens());
    }

    private RateLimited getRateLimitedConfig() throws Exception {

        var method = TestController.class.getDeclaredMethod("testMethod");

        return method.getAnnotation(RateLimited.class);
    }

    static class TestController {

        @RateLimited(capacity = 5, tokens = 5, unit = ChronoUnit.MINUTES)
        public void testMethod() {
        }
    }
}