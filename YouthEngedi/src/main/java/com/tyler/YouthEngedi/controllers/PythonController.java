package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import com.tyler.YouthEngedi.services.PythonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/python")
public class PythonController {

    private final PythonService pythonService;

    public PythonController(PythonService pythonService){
        this.pythonService = pythonService;
    }

    @GetMapping("/predict")
    public ResponseEntity<PredictionResponse> getPrediction(){
        try{
            return ResponseEntity.ok(pythonService.getPrediction());
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(new PredictionResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
