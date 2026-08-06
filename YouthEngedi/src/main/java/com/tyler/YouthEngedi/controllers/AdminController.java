package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.Performance;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getSystemLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        return ResponseEntity.ok(adminService.getSystemLogs(page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/performances")
    public ResponseEntity<Page<Performance>> getSystemPerformanceData(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        return ResponseEntity.ok(adminService.getSystemPerformanceData(page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("test-email/{type}")
    public ResponseEntity<ApiResult> sendTestEmail(@PathVariable String type){
        try{
            adminService.sendTestEmail(type);
            return ResponseEntity.ok(new ApiResult(true,"Test email sent successfully"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


}
