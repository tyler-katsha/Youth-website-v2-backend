package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.*;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.Repository.VerificationTokenRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.VerificationToken;
import com.tyler.YouthEngedi.models.dtos.SocialLoginRequest;
import com.tyler.YouthEngedi.models.dtos.UserLoginRequest;
import com.tyler.YouthEngedi.models.dtos.UserRegisterRequest;
import com.tyler.YouthEngedi.services.EmailService;
import com.tyler.YouthEngedi.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @PostMapping(value="/register", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@ModelAttribute UserRegisterRequest request){
        try{

            boolean valid = emailService.hasMXRecord(request.getEmail());

            if(!valid){
                throw new InvalidEmailException("Email doesn't exist");
            }

            return userService.register(request);

        } catch (InvalidEmailException e){
            return new ResponseEntity<>("Email doesn't exist",HttpStatus.IM_USED);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Resource not found",HttpStatus.NOT_FOUND);
        } catch (ImageException e){
            return new ResponseEntity<>("Image too big",HttpStatus.CONTENT_TOO_LARGE);
        } catch (AuthorizationException e){
            return new ResponseEntity<>("Email already exist",HttpStatus.CONFLICT);
        } catch (Exception e){
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request){
        try{
            return userService.login(request);
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

    @PostMapping("/social")
    public ResponseEntity<?> loginWithOAuth2(@RequestBody SocialLoginRequest request){
        try{
            return userService.loginWithOAuth2(request);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to login",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/continue-as-guest")
    public ResponseEntity<?> continueAsGuest(){
        try{
            return userService.continueAsGuest();
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to continue as guest",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return userService.logout(response, principal.getUserId());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to logout",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> resetPassword(@AuthenticationPrincipal UserPrincipal principal,@RequestBody String password){
        try{
            userService.resetPassword(principal.getUserId(),password);
            return new ResponseEntity<>("Verify sent to inbox",HttpStatus.OK);
        } catch(PasswordResetException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>("Failed to verify user",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }
}
