package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Details about an document")
public class RoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roleReqId;
    @Enumerated(EnumType.STRING)
    private Role requestedRole;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewAt;
    private String adminComment;
    private String userReason;
    private boolean wasReviewed = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_by")
    private User reviewedBy;
}
