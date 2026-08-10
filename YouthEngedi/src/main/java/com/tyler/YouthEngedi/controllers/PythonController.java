package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import com.tyler.YouthEngedi.services.PythonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/python")
public class PythonController {

    private final PythonService pythonService;

    public PythonController(PythonService pythonService){
        this.pythonService = pythonService;
    }

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> getPrediction(@RequestBody PredictionRequest request){
        try{
            return ResponseEntity.ok(pythonService.getPrediction(request));
        } catch (Exception e){
            return new ResponseEntity<>(PredictionResponse.builder().detections(List.of()).approved(false).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
