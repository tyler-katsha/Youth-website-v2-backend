package com.tyler.YouthEngedi.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Schema(description = "All information about a verification token")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Creates a UUID object for each token that's generate",example = "123456789876543sdfsgsdsdkfojfksjfl")
    private UUID verificationTokenId;
    @Schema(description = "Creates a unique token to generate for each user",example = "token123")
    private String token;
    @ManyToOne
    @JoinColumn(name= "user_id")
    @Schema(description = "Stores the users primary key in the Verification token table",example = "1")
    private User user;
    private LocalDateTime expiryDate;
}
