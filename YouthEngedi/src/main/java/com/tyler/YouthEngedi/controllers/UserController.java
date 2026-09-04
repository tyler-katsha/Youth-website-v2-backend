package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.RateLimitExceededException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.ProfileRequest;
import com.tyler.YouthEngedi.models.dtos.UserResponse;
import com.tyler.YouthEngedi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.temporal.ChronoUnit;


@RestController
@RequestMapping("/api/v1")
@Tag(name=" User Management", description = "Api for fetching and managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @GetMapping("/users")
    @Operation(summary = "Finds all the users using Pagination",description = "Fetches all the users in the database")
    @ApiResponse(responseCode="200",description="Users was found successfully.")
    @ApiResponse(responseCode="404",description="An error was thrown maybe like a database offline.")
    @ApiResponse(responseCode="400",description="Users was not found in the database")
    @ApiResponse(responseCode="500",description="An generic exception was thrown and something went wrong")
    public ResponseEntity<Page<UserResponse>> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return ResponseEntity.ok(userService.findAll(page,size));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/users/me")
    @Operation(summary = "Finds a user based on there user id that the user logged with and injects it into the jwt token",description = "Fetches user based on there unique ID")
    @ApiResponse(responseCode = "200",description = "Successfully finds users profile details")
    @ApiResponse(responseCode = "404",description = "User information was not found")
    @ApiResponse(responseCode = "500",description = "An error has occurred with the system")
    public ResponseEntity<?> findUserProfile(@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(userService.findById(principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    @PutMapping(value = "/users/update-me",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Updates a user's profile information",description = "Fetches user data from the request body and updates the user's attributes")
    @ApiResponse(responseCode = "200",description = "User's details was successfully added to the database")
    @ApiResponse(responseCode = "404",description = "User was not found based with there ID")
    @ApiResponse(responseCode = "400",description = "User data was inserted properly into the ProfileRequest body")
    @ApiResponse(responseCode = "500",description = "An error has occurred with the system")
    public ResponseEntity<?> updateProfile(@ModelAttribute ProfileRequest request, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(userService.updateProfile(request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/role/{email}/upgrade")
    @Operation(summary = "Finds user based on email and adds the next role if they are not admin already.",description = "This uses a method to add a level higher than the current one and returns the next level and adds it to the set and sets the users roles into a hashSet.")
    @ApiResponse(responseCode = "200",description = "User's Set of roles was successfully updated with some edge cases included. Role/s were added.")
    @ApiResponse(responseCode = "404",description = "User was not found based with there email")
    @ApiResponse(responseCode = "400",description = "An error has occurred with the system")
    public ResponseEntity<?> upgradeMemberRole(@PathVariable String email){
        try{
            return new ResponseEntity<>(userService.upgradeMemberRole(email),HttpStatus.OK);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/role/{email}/downgrade")
    @Operation(summary = "Finds user based on email and removes the highest role level if they are not a member role already.",description = "This uses a method to removes a current level higher and returns the a set with the previous roles and sets the users with one less role if the user is not a regular member role.")
    @ApiResponse(responseCode = "200",description = "User's Set of roles was successfully updated with some edge cases included. Role/s were removed.")
    @ApiResponse(responseCode = "404",description = "User was not found based with there email")
    @ApiResponse(responseCode = "400",description = "An error has occurred with the system")
    public ResponseEntity<?> downgradeMemberRole(@PathVariable String email){
        try{
            return new ResponseEntity<>(userService.downgradeMemberRole(email),HttpStatus.OK);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users")
    @Operation(summary = "Uses the user-id to hard delete the user",description = "This removes the user account from the database and anything related to the user")
    @ApiResponse(responseCode = "200",description = "Hard deletes the user's account from the system")
    @ApiResponse(responseCode = "404",description = "User was not found based with there user-id")
    @ApiResponse(responseCode = "400",description = "An error has occurred with the system")
    @ApiResponse(responseCode = "403",description = "Token is not found or expired")
    public ResponseEntity<ApiResult> deleteAccount(@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(new ApiResult(true, userService.deleteAccount(principal.getUserId())));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    @PutMapping("/users/{email}/deactivate")
    @Operation(summary = "Uses the user-id to soft delete the user",description = "This disables the user account")
    @ApiResponse(responseCode = "200",description = "User account was successfully disabled.")
    @ApiResponse(responseCode = "404",description = "User was not found based with there user-id")
    @ApiResponse(responseCode = "400",description = "An error has occurred with the system")
    public ResponseEntity<ApiResult> deactivateMember(@PathVariable String email){
        try{
            return ResponseEntity.ok(new ApiResult(true,userService.deactivateMember(email)));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{email}/activate")
    @ApiResponse(responseCode = "200",description = "User account was successfully enabled.")
    @Operation(summary = "Uses the user-id to soft delete the user",description = "This disables the user account")
    @ApiResponse(responseCode = "404",description = "User was not found based with there user-id")
    @ApiResponse(responseCode = "400",description = "An error has occurred with the system")
    public ResponseEntity<ApiResult> activateMember(@PathVariable String email){
        try{
            return ResponseEntity.ok(new ApiResult(true,userService.activateMember(email)));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
}
