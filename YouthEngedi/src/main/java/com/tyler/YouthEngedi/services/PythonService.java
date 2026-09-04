package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

@Service
public class PythonService {

    private final CircuitBreakerFactory<?,?> circuitBreakerFactory;
    private final WebClient webClient;

    public PythonService(WebClient webClient,CircuitBreakerFactory<?,?> circuitBreakerFactory){
        this.webClient = webClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Retryable(maxRetries = 3,delay = 1000,multiplier = 2,includes = {Exception.class})
    public PredictionResponse getPrediction(PredictionRequest request){

        var circuitBreaker = circuitBreakerFactory.create("pythonService");

        return circuitBreaker.run(() -> webClient.post()
                .uri(production ? PYTHON_PREDICTION_PROD : PYTHON_PREDICTION_DEV)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .block());
    }

    @Recover
    public PredictionResponse predictionFallback(Throwable throwable,PredictionRequest request){
        return PredictionResponse.builder()
                .approved(true)
                .detections(List.of())
                .build();
    }
}
