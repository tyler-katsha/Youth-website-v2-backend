package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.PasswordResetException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.EmailRequest;
import com.tyler.YouthEngedi.models.dtos.PartialPasswordResetRequest;
import com.tyler.YouthEngedi.services.EmailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static com.tyler.YouthEngedi.constants.UrlConstants.FRONTEND_RESET_PASSWORD_DEV;
import static com.tyler.YouthEngedi.constants.UrlConstants.FRONTEND_RESET_PASSWORD_PROD;
import static com.tyler.YouthEngedi.services.CookieService.production;

//import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Tag(name="Email Management",description = "Api for sending emails to one or all users")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;
//    private final VerificationTokenService verificationTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send-email")
    @Operation(summary = "Sends a email to the developer for an inquiry")
    @ApiResponse(responseCode = "200",description = "Send's a confirmation message back to the user")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request){
        try{
            CompletableFuture.runAsync(() -> {
                emailService.sendEmail(request);
            });
            return ResponseEntity.ok("Email sent.");
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Send a partial request of the user that want's to reset there password")
    @ApiResponse(responseCode = "200",description = "The url is sent back to the user successfully")
    @ApiResponse(responseCode = "404",description = "User is not found based on email")
    @ApiResponse(responseCode = "401",description = "Token is expired/invalid")
    @ApiResponse(responseCode = "403",description = "Unable to access that endpoint")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> sendEmail(@RequestBody PartialPasswordResetRequest request){
        try{

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));
            String token = jwtTokenProvider.generateToken(user);

//            PasswordResetRequest fullRequest = PasswordResetRequest.builder().token(token).email(request.getEmail()).build();
//            CompletableFuture.runAsync(() -> {
//                try{
//                    emailService.sendEmail(fullRequest);
//                } catch (MessagingException e){
//                    e.printStackTrace();
//                }
//            });

            String url = String.format(production ? FRONTEND_RESET_PASSWORD_PROD : FRONTEND_RESET_PASSWORD_DEV, token,request.getEmail());
            return ResponseEntity.ok(url);

        } catch(ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false, e.getMessage()),HttpStatus.NOT_FOUND);
        } catch(PasswordResetException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.FORBIDDEN);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>(new ApiResult(false,"Token is expired"),HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//    @PostMapping("/resend-verification")
//    public ResponseEntity<?> resendVerification(String email){
//        verificationTokenService.resendVerification(email);
//        return new ResponseEntity<>("Resend verification email",HttpStatus.OK);
//    }
}
