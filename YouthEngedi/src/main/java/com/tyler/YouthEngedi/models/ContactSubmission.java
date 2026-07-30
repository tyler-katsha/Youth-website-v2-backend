package com.tyler.YouthEngedi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long contactSubmissionId;
    private String senderName;
    private String senderEmail;
    private String subject;
    private String message;
    private LocalDateTime submittedAt;
}
