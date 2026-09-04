package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.ContactSubmissionRepository;
import com.tyler.YouthEngedi.models.ContactSubmission;
import com.tyler.YouthEngedi.models.dtos.EmailRequest;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private ContactSubmissionRepository contactSubmissionRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<ContactSubmission> submissionCaptor;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    private String testDeveloperEmail;

    @BeforeEach
    void setUp() {
        testDeveloperEmail = "dev@gmail.com";
        // Injects the @Value("${spring.mail.youth.email}") property
        ReflectionTestUtils.setField(emailService, "adminEmail", testDeveloperEmail);
    }

    @Nested
    @DisplayName("sendEmail Tests")
    class SendEmailTests {

        @Test
        @DisplayName("Should successfully persist contact submission and send MIME email")
        void sendEmail_Success() throws Exception {
            EmailRequest request = EmailRequest.builder()
                    .name("Jane Doe")
                    .email("jane@example.com")
                    .message("Hello from the contact form!")
                    .build();

            // Provide an actual MimeMessage backed by an empty Session so MimeMessageHelper functions properly
            MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            emailService.sendEmail(request);

            // 1. Verify entity saved in repository
            verify(contactSubmissionRepository, times(1)).save(submissionCaptor.capture());
            ContactSubmission savedSubmission = submissionCaptor.getValue();
            assertEquals("Jane Doe", savedSubmission.getSenderName());
            assertEquals("jane@example.com", savedSubmission.getSenderEmail());
            assertEquals("Hello from the contact form!", savedSubmission.getMessage());
            assertNotNull(savedSubmission.getSubmittedAt());

            // 2. Verify mailSender sent the message with expected parameters
            verify(mailSender, times(1)).send(mimeMessageCaptor.capture());
            MimeMessage sentMessage = mimeMessageCaptor.getValue();

            assertEquals("New Contact Message from Jane Doe", sentMessage.getSubject());
            assertEquals(1, sentMessage.getAllRecipients().length);
            assertEquals("jane@example.com", sentMessage.getAllRecipients()[0].toString());
            assertEquals(testDeveloperEmail, sentMessage.getFrom()[0].toString());
            assertEquals("jane@example.com", sentMessage.getReplyTo()[0].toString());
        }

        @Test
        @DisplayName("Should still persist contact submission even if mail sending throws an exception")
        void sendEmail_MailExceptionHandledGracefully() {
            EmailRequest request = EmailRequest.builder()
                    .name("John Smith")
                    .email("john@example.com")
                    .message("Testing failure scenario")
                    .build();

            MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new RuntimeException("SMTP Host unavailable")).when(mailSender).send(any(MimeMessage.class));

            // Should catch and not propagate unhandled exceptions up the call stack
            assertDoesNotThrow(() -> emailService.sendEmail(request));

            verify(contactSubmissionRepository, times(1)).save(any(ContactSubmission.class));
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("hasMXRecord Tests")
    class HasMXRecordTests {

        @Test
        @DisplayName("Should return false for malformed or missing email domain")
        void hasMXRecord_MalformedEmailReturnsFalse() {
            assertFalse(emailService.hasMXRecord("not-an-email"));
            assertFalse(emailService.hasMXRecord("invalid@"));
        }

        @Test
        @DisplayName("Should return false for non-existent domain")
        void hasMXRecord_NonExistentDomainReturnsFalse() {
            // Unresolvable domain should return false without uncaught exceptions
            boolean result = emailService.hasMXRecord("user@thisdomaindoesnotexist1234567890abcdefg.org");
            assertFalse(result);
        }
    }
}