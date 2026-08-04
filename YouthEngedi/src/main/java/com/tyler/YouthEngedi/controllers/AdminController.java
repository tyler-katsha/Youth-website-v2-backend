package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.Performance;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
        return adminService.getSystemLogs(page,size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/performances")
    public ResponseEntity<Page<Performance>> getSystemPerformanceData(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        return adminService.getSystemPerformanceData(page,size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivateUser(@RequestBody String email){
        try{
            return ResponseEntity.ok(adminService.deactivateUser(email));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestBody String email){
        try{
            return ResponseEntity.ok(adminService.activateUser(email));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("test-email/{type}")
    public ResponseEntity<ApiResponse> sendTestEmail(@PathVariable String type){
        try{
            adminService.sendTestEmail(type);
            return ResponseEntity.ok(new ApiResponse(true,"Test email sent successfully"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


}
