package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

@Service
public class PythonService {

    private static final Logger logger = LoggerFactory.getLogger(PythonService.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final String targetUri;
    private final LocalNsfwFallbackService localNsfwFallbackService;

    public PythonService(CircuitBreakerFactory<?, ?> circuitBreakerFactory,LocalNsfwFallbackService localNsfwFallbackService) {

        // Enforce strict timeouts so slow requests don't exhaust the Tomcat thread pool
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(1000));
        requestFactory.setReadTimeout(Duration.ofMillis(2000));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        this.circuitBreaker = circuitBreakerFactory.create("pythonService");
        this.targetUri = production ? PYTHON_PREDICTION_PROD : PYTHON_PREDICTION_DEV;
        this.localNsfwFallbackService = localNsfwFallbackService;
    }

    public PredictionResponse getPrediction(PredictionRequest request) {
        return circuitBreaker.run(
                () -> restClient.post()
                        .uri(targetUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(PredictionResponse.class),

                throwable -> predictionFallback(throwable, request)
        );
    }

    private PredictionResponse predictionFallback(Throwable throwable, PredictionRequest request) {
        logger.warn("Python prediction service unavailable ({}). Triggering fallback.", throwable.toString());

        // Fail-closed to avoid accidental security/validation exploits during outages

        boolean isSafe = false;
        try {
            // Guard fallback execution so failures here don't bubble unhandled 500s
            isSafe = localNsfwFallbackService.isSafe(request.getPath());
        } catch (Exception ex) {
            logger.error("Local NSFW fallback failed for path: {}. Defaulting to fail-closed.", request.getPath(), ex);
        }

        logger.debug("Local NSFW fallback outcome: approved={} for path={}", isSafe, request.getPath());

        return PredictionResponse.builder()
                .approved(isSafe)
                .detections(List.of())
                .build();
    }
}