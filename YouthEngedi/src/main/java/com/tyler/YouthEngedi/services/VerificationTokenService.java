package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.Repository.VerificationTokenRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.VerificationToken;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class VerificationTokenService {
    private final static Logger logger = LogManager.getLogger(VerificationTokenService.class);
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Async
    public void sendVerificationLink(User user){
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);

        CompletableFuture.runAsync(() -> {
            try{
                emailService.sendVerificationEmail(user.getEmail(),token);
            } catch (MessagingException e){
                logger.error("Failed to send email to {} ",user.getEmail(),e);
            }
        });
    }
    public void resendVerification(String email){
        String token = UUID.randomUUID().toString();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);

        try{
            emailService.sendVerificationEmail(email,token);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} ",email,e);
        }
    }
}
