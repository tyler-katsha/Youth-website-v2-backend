package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.PasswordResetException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.PasswordResetRequest;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.models.dtos.EmailRequest;
import com.tyler.YouthEngedi.models.dtos.PartialPasswordResetRequest;
import com.tyler.YouthEngedi.services.EmailService;
import com.tyler.YouthEngedi.services.EventService;
import com.tyler.YouthEngedi.services.VerificationTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {
    private final static Logger logger = LogManager.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenService verificationTokenService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request){
        try{
            emailService.sendEmail(request);
            return new ResponseEntity<>("Email sent.",HttpStatus.OK);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} with subject {}",request.getEmail(),request.getSubject(),e);
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> sendEmail(@RequestBody PartialPasswordResetRequest request){
        try{

            String token = jwtTokenProvider.generateToken(request.getEmail());

            PasswordResetRequest fullRequest = PasswordResetRequest.builder().token(token).email(request.getEmail()).build();

            CompletableFuture.runAsync(() -> {
                try{
                    emailService.sendEmail(fullRequest);
                } catch (MessagingException e){
                    e.printStackTrace();
                }
            });
            return ResponseEntity.ok("Email sent successfully");

        } catch(PasswordResetException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>(new ApiResponse(false,"Token is expired"),HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(String email){
        verificationTokenService.resendVerification(email);
        return new ResponseEntity<>("Resend verification email",HttpStatus.OK);
    }
}
