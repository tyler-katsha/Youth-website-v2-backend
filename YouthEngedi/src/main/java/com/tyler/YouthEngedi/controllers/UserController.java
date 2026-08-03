package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.models.dtos.ProfileRequest;
import com.tyler.YouthEngedi.models.dtos.UserResponse;
import com.tyler.YouthEngedi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return userService.findAll(page,size);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/users/me")
    public ResponseEntity<?> findUserProfile(@AuthenticationPrincipal UserPrincipal principal){
        try{
            return userService.findById(principal.getUserId());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    @PutMapping(value = "/users/update-me",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@ModelAttribute ProfileRequest request, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return userService.updateProfile(request,principal.getUserId());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/role/{email}/upgrade")
    public ResponseEntity<?> upgradeMemberRole(@PathVariable String email){
        try{
            userService.upgradeMemberRole(email);
            return new ResponseEntity<>("Upgraded user",HttpStatus.OK);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/role/{email}/downgrade")
    public ResponseEntity<?> downgradeMemberRole(@PathVariable String email){
        try{
            userService.downgradeMemberRole(email);
            return new ResponseEntity<>("Downgraded user",HttpStatus.OK);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @DeleteMapping("/users")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserPrincipal principal,@CookieValue(name="jwt-token") String token){
        try{

            if(token == null){
                return new ResponseEntity<>(new ApiResponse(false,"Access Denied! Token missing!"),HttpStatus.FORBIDDEN);
            }

            return userService.deleteAccount(principal.getUserId(),token);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{email}/deactivate")
    public ResponseEntity<?> deactivateMember(@PathVariable String email){
        try{
            return userService.deactivateMember(email);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{email}/activate")
    public ResponseEntity<?> activateMember(@PathVariable String email){
        try{
            return userService.activateMember(email);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
}
