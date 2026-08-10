package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;
import static com.tyler.YouthEngedi.services.CookieService.production;

@Service
public class PythonService {

    private final WebClient webClient;

    public PythonService(WebClient webClient){
        this.webClient = webClient;
    }

    public PredictionResponse getPrediction(PredictionRequest request){

        return webClient.post()
                .uri(production ? PYTHON_PREDICTION_PROD : PYTHON_PREDICTION_DEV)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .block();
    }
}
