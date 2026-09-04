package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.InvalidEmailException;
import com.tyler.YouthEngedi.Repository.ContactSubmissionRepository;
import com.tyler.YouthEngedi.models.ContactSubmission;
import com.tyler.YouthEngedi.models.dtos.EmailRequest;
import com.tyler.YouthEngedi.utils.HtmlTemplate;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Value("${spring.mail.youth.email}")
    private String adminEmail;

    private final ContactSubmissionRepository contactSubmissionRepository;
    private final JavaMailSender mailSender;

    public EmailService(ContactSubmissionRepository contactSubmissionRepository, JavaMailSender mailSender) {
        this.contactSubmissionRepository = contactSubmissionRepository;
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(EmailRequest request) {
        String subject = String.format("New Contact Message from %s", request.getName());
        String emailBody = HtmlTemplate.basicEmailRequest(request.getName(), request.getEmail(), request.getMessage());

        ContactSubmission contactSubmission = ContactSubmission.builder()
                .senderEmail(request.getEmail())
                .senderName(request.getName())
                .message(request.getMessage())
                .submittedAt(LocalDateTime.now())
                .build();

        contactSubmissionRepository.save(contactSubmission);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.getEmail());
            helper.setFrom(adminEmail);
            helper.setReplyTo(request.getEmail());
            helper.setSubject(subject);
            helper.setText(emailBody, true);

            mailSender.send(message);
        } catch (Exception ignore) {}
    }

    public boolean hasMXRecord(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        try {
            String domain = email.substring(email.indexOf("@") + 1).trim();
            if (domain.isEmpty()) {
                return false;
            }

            Lookup lookup = new Lookup(domain, Type.MX);
            Record[] records = lookup.run();

            return records != null && records.length > 0;
        } catch (TextParseException | IllegalArgumentException e) {
            return false;
        }
    }
}