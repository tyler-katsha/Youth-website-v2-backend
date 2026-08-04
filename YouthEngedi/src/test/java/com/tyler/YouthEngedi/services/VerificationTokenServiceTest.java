package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.Repository.VerificationTokenRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.VerificationToken;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationTokenServiceTest Unit test")
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationTokenService verificationTokenService; // this is what we want to test

    private User testUser;
    private VerificationToken savedToken;

    @BeforeEach
    void setUp(){
        testUser = User.builder()
                .email("test123@gmail.com")
                .name("Bob")
                .bio("Welcome")
                .build();
        savedToken = VerificationToken.builder()
                .token("12345ABC")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
    }

    @DisplayName("Send verification link Tests")
    @Nested
    class VerificationLinkTest{

        @Test
        @DisplayName("Should send email successfully when valid email exists in the user database table")
        void shouldSendEmailSuccessfullyWhenValidEmailExistsInTheUserDatabaseTable() throws MessagingException {

            when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(savedToken);

            verificationTokenService.sendVerificationLink(testUser);

            verify(verificationTokenRepository, atLeast(1)).deleteByUser(testUser);
            verify(verificationTokenRepository, atLeast(1)).save(any(VerificationToken.class));

            verify(emailService, atLeast(1)).sendVerificationEmail(eq(testUser.getEmail()),anyString());

        }

        @Test
        @DisplayName("Should throw an exception when email is empty")
        void shouldThrowAnExceptionWhenEmailIsEmpty(){

            testUser.setEmail(null);

            assertThrowsExactly(IllegalArgumentException.class,() -> verificationTokenService.sendVerificationLink(testUser));

            verifyNoInteractions(emailService);

        }
    }

    @DisplayName("Resend Verification Link Tests")
    @Nested
    class ResendVerificationLink{

        @Test
        @DisplayName("Should remove current verification entity and send a new email and create a new entity in the database")
        void shouldRemoveCurrentVerificationEntityAndSendANewEmailAndCreateANewEntityInTheDatabase() throws MessagingException {

            verificationTokenService.resendVerification(testUser.getEmail());

            verify(verificationTokenRepository).deleteByUser(testUser);
            verify(verificationTokenRepository).save(any(VerificationToken.class));

            verify(emailService).sendVerificationEmail(eq(testUser.getEmail()),anyString());
        }

    }


}