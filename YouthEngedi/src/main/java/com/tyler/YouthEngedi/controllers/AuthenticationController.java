package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.*;
import com.tyler.YouthEngedi.Repository.VerificationTokenRepository;
import com.tyler.YouthEngedi.models.PasswordResetRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.VerificationToken;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.UserLoginRequest;
import com.tyler.YouthEngedi.models.dtos.UserRegisterRequest;
import com.tyler.YouthEngedi.services.CookieService;
import com.tyler.YouthEngedi.services.EmailService;
import com.tyler.YouthEngedi.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name="Authentication Management",description = "Api for managing non-secure based endpoints")
public class AuthenticationController {

    private final UserService userService;
    private final EmailService emailService;
//    private final CookieService cookieService;
    private final VerificationTokenRepository verificationTokenRepository;

    @PostMapping(value="/register", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registers user to the system")
    @ApiResponse(responseCode = "201",description = "Successful creates user")
    @ApiResponse(responseCode = "400",description = "Email is in invalid format")
    @ApiResponse(responseCode = "409",description = "Email already exist")
    @ApiResponse(responseCode = "404",description = "User not found")
    @ApiResponse(responseCode = "413",description = "Image is too large")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> register(@ModelAttribute UserRegisterRequest request){
        try{

            boolean valid = emailService.hasMXRecord(request.getEmail());

            if(!valid){
                throw new InvalidEmailException("Email doesn't exist");
            }

            return new ResponseEntity<>(new ApiResult(true,userService.register(request)), HttpStatus.CREATED);

        } catch (InvalidEmailException e){
            return new ResponseEntity<>(new ApiResult(false,"Email domain doesn't exist"),HttpStatus.BAD_REQUEST);
        } catch (AuthorizationException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.CONFLICT);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Resource not found"),HttpStatus.NOT_FOUND);
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Image too large"),HttpStatus.CONTENT_TOO_LARGE);
        }  catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Log user into the system")
    @ApiResponse(responseCode = "200",description = "Successful login user")
    @ApiResponse(responseCode = "409",description = "Email is in invalid format")
    @ApiResponse(responseCode = "403",description = "Credentials don't match either email or password")
    @ApiResponse(responseCode = "404",description = "User not found")
    @ApiResponse(responseCode = "423",description = "User account is not enabled")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request){
        try{
            return ResponseEntity.ok(userService.login(request));
        } catch (InvalidEmailException e){
            return new ResponseEntity<>("Invalid email",HttpStatus.CONFLICT);
        } catch(AuthorizationException e){
            return new ResponseEntity<>("Invalid credentials",HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Resource not found",HttpStatus.NOT_FOUND);
        } catch(LockedAccountException e) {
            return new ResponseEntity<>("Account is locked",HttpStatus.LOCKED);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/continue-as-guest")
    @Operation(summary = "Login as guest",description = "Creates a shorten version of a token to allow this type of user to access limited features")
    @ApiResponse(responseCode = "200",description = "Successful continue as guest")
    @ApiResponse(responseCode = "204",description = "Failed to create jwt token not found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> continueAsGuest(){
        try{
            return ResponseEntity.ok(userService.continueAsGuest());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to continue as guest",HttpStatus.NO_CONTENT);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/logout")
    @Operation(summary = "Log user out of system",description = "Destroys the Jwt-Token stored in that login period")
    @ApiResponse(responseCode = "200",description = "Successful logs out user")
    @ApiResponse(responseCode = "204",description = "Token not found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> logout(HttpServletResponse response){
        try{
            return ResponseEntity.ok(userService.logout(response));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to logout",HttpStatus.NO_CONTENT);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "Display's a Verification Page", description = "Display whether the user is successfully verified or failed to verify and enable the user's account")
    @ApiResponse(responseCode = "200",description = "User is verified successfully")
    @ApiResponse(responseCode = "204",description = "Verification entity was not found")
    @ApiResponse(responseCode = "406",description = "Jwt-Token doesn't match")
    @ApiResponse(responseCode = "400",description = "Token Expired")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<String> verify(@RequestParam("token") String token){
        try{
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

            if(verificationToken == null){
                return new ResponseEntity<>("Token is doesn't match",HttpStatus.NOT_ACCEPTABLE);
            }
            if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
                return ResponseEntity.badRequest().body("Token expired");
            }

            User user = verificationToken.getUser();

            CompletableFuture.runAsync(() -> {
                userService.enableMember(user);
            });

            verificationTokenRepository.delete(verificationToken);

            return ResponseEntity.ok("Email verified successfully");
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to verify user",HttpStatus.NO_CONTENT);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Resets the user password",description = "Resets the user old password and adds the new one to the database")
    @ApiResponse(responseCode = "200",description = "Password is reset successfully")
    @ApiResponse(responseCode = "406",description = "Password doesn't match requirements")
    @ApiResponse(responseCode = "503",description = "Jwt-Token is expired")
    @ApiResponse(responseCode = "203",description = "User email not found or Guest email not found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request){
        try{
            userService.resetPassword(request);
            return new ResponseEntity<>("Password reset successfully.",HttpStatus.OK);
        } catch(PasswordResetException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_ACCEPTABLE);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to verify user",HttpStatus.NO_CONTENT);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>("Session is expired",HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
