package com.tyler.YouthEngedi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health Management",description = "Api for fetching system related information")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Get's the health of the application",description = "Getting to replace with Spring Actuators soon")
    @ApiResponse(responseCode = "200",description = "Returns OK")
    public ResponseEntity<String> healthCheck(){
        return ResponseEntity.ok("OK");
    }
}
