package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.Repository.VerificationTokenRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.VerificationToken;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void sendVerificationLink(User user){
        String token = UUID.randomUUID().toString();

        verificationTokenRepository.deleteByUser(user);
        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);

        sendTokenEmail(user.getEmail(),token);
    }

    @Transactional
    public void resendVerification(String email){
        String token = UUID.randomUUID().toString();


        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        System.out.println("User: " + user);
        verificationTokenRepository.deleteByUser(user);

        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token).user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();

        verificationTokenRepository.save(verificationToken);

        sendTokenEmail(email,token);
    }


    @Async
    private void sendTokenEmail(String email,String token){
        CompletableFuture.runAsync(() -> {
            try{
                emailService.sendVerificationEmail(email,token);
            } catch (MessagingException e){
                logger.error("Failed to send email to {} ",email,e);
            }
        });
    }
}

