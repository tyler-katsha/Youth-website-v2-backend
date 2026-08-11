package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.ContactSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceTest class Unit Tests")
class EmailServiceTest {
    @Mock
    private ContactSubmissionRepository contactSubmissionRepository;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;


    private String testDeveloperEmail;

    @BeforeEach
    void setUp(){
        testDeveloperEmail = "dev@gmail.com";
    }
}