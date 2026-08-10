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
    public ResponseEntity<ApiResult> register(@ModelAttribute UserRegisterRequest request){
        try{

            boolean valid = emailService.hasMXRecord(request.getEmail());

            if(!valid){
                throw new InvalidEmailException("Email doesn't exist");
            }

            return new ResponseEntity<>(new ApiResult(true,userService.register(request)), HttpStatus.CREATED);

        } catch (InvalidEmailException e){
            return new ResponseEntity<>(new ApiResult(false,"Email domain doesn't exist"),HttpStatus.IM_USED);
        } catch (AuthorizationException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.CONFLICT);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Resource not found"),HttpStatus.NOT_FOUND);
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Image too big"),HttpStatus.CONTENT_TOO_LARGE);
        }  catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again"),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Log user into the system")
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
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/continue-as-guest")
    @Operation(summary = "Login as guest",description = "Creates a shorten version of a token to allow this type of user to access limited features")
    public ResponseEntity<?> continueAsGuest(){
        try{
            return ResponseEntity.ok(userService.continueAsGuest());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to continue as guest",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/logout")
    @Operation(summary = "Log user out of system",description = "Destroys the Jwt-Token stored in that login period")
    public ResponseEntity<?> logout(HttpServletResponse response){
        try{
            return ResponseEntity.ok(userService.logout(response));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to logout",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verify")
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
            return new ResponseEntity<>("Failed to verify user",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "")
    @ApiResponse(responseCode = "200",description = "")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request){
        try{
            userService.resetPassword(request);
            return new ResponseEntity<>("Password reset successfully.",HttpStatus.OK);
        } catch(PasswordResetException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_ACCEPTABLE);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to verify user",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>("Session is expired",HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong. Please try again",HttpStatus.BAD_REQUEST);
        }
    }
}
