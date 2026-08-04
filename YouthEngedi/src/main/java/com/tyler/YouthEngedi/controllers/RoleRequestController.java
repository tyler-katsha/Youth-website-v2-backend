package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import com.tyler.YouthEngedi.services.RoleRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoleRequestController {

    private final RoleRequestService roleRequestService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<?> findAllRoleRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return roleRequestService.findAllRoleRequests(page,size);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/send-request")
    public ResponseEntity<?> sendRoleRequest(@AuthenticationPrincipal UserPrincipal principal){
        try{
            return roleRequestService.sendRoleRequest(principal.getUserId());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-request")
    public ResponseEntity<?> updateRoleRequest(@RequestBody RoleChangeRequest request){
        try{
            roleRequestService.updateRequest(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
