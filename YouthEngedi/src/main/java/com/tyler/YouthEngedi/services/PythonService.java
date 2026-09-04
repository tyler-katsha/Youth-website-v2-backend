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

    public PythonService(CircuitBreakerFactory<?, ?> circuitBreakerFactory) {

        // Enforce strict timeouts so slow requests don't exhaust the Tomcat thread pool
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(1000));
        requestFactory.setReadTimeout(Duration.ofMillis(2000));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        this.circuitBreaker = circuitBreakerFactory.create("pythonService");
        this.targetUri = production ? PYTHON_PREDICTION_PROD : PYTHON_PREDICTION_DEV;
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
        logger.warn("Python prediction call failed or circuit is open: {}", throwable.getMessage());

        // Fail-closed to avoid accidental security/validation exploits during outages
        return PredictionResponse.builder()
                .approved(false)
                .detections(List.of())
                .build();
    }
}