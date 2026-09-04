package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import com.tyler.YouthEngedi.services.PythonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/python")
public class PythonController {

    private final PythonService pythonService;

    public PythonController(PythonService pythonService){
        this.pythonService = pythonService;
    }


    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/predict")
    @Operation(summary = "Get's a prediction score from python",description = "Returns a prediction using Python ML model")
    @ApiResponse(responseCode = "200",description = "Prediction score is and approved is sent to spring")
    @ApiResponse(responseCode = "500",description = "An empty PredictionResponse is sent to spring")
    public ResponseEntity<PredictionResponse> getPrediction(@RequestBody PredictionRequest request){
        try{
            return ResponseEntity.ok(pythonService.getPrediction(request));
        } catch (Exception e){
            return new ResponseEntity<>(PredictionResponse.builder().detections(null).approved(false).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
