package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Exceptions.RoleRequestPendingException;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import com.tyler.YouthEngedi.services.RoleRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="Role Request Management",description = "Api for fetching and managing role requests")
public class RoleRequestController {

    private final RoleRequestService roleRequestService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<?> findAllRoleRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return ResponseEntity.ok(roleRequestService.findAllRoleRequests(page,size));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/send-request")
    public ResponseEntity<ApiResult> sendRoleRequest(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            roleRequestService.sendRoleRequest(principal.getUserId());
            return ResponseEntity.ok(new ApiResult(true,"Role request was sent to the admins"));
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(new ApiResult(false, e.getMessage()), HttpStatus.NOT_FOUND);
        } catch(RoleRequestPendingException e){
            return new ResponseEntity<>(new ApiResult(false,"Request is still being processed"),HttpStatus.CONFLICT);
        }catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-request")
    public ResponseEntity<ApiResult> updateRoleRequest(@RequestBody RoleChangeRequest request){
        try{
            roleRequestService.updateRequest(request);

            return ResponseEntity.ok(new ApiResult(true,"Role request was updated successfully."));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
