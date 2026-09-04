package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

@Service
public class PythonService {

    private final CircuitBreakerFactory<?,?> circuitBreakerFactory;
    private final WebClient webClient;

    public PythonService(WebClient webClient,CircuitBreakerFactory<?,?> circuitBreakerFactory){
        this.webClient = webClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PredictionResponse getPrediction(PredictionRequest request){

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("pythonService");
        return circuitBreaker.run(() -> {
            return webClient.post()
                    .uri(production ? PYTHON_PREDICTION_PROD : PYTHON_PREDICTION_DEV)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PredictionResponse.class)
                    .block();
        },throwable -> predictionFallback());
    }

    public PredictionResponse predictionFallback(){
        return new PredictionResponse();
    }
}
