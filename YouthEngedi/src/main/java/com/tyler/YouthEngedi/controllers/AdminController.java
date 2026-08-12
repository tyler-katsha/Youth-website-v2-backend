package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.Performance;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name="Admin Management",description = "Api fetching and managing admin based features strictly")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    @Operation(summary = "Finds all the system logs using Pagination",description = "Fetches all the logs in the database")
    @ApiResponse(responseCode = "200",description = "Get's all system logs")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> getSystemLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return ResponseEntity.ok(adminService.getSystemLogs(page,size));
        } catch(Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/performances")
    @Operation(summary = "Finds all the system performance data using Pagination",description = "Fetches all the performances data in the database")
    @ApiResponse(responseCode = "200",description = "Get's all system performance data")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> getSystemPerformanceData(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return ResponseEntity.ok(adminService.getSystemPerformanceData(page,size));
        } catch(Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
