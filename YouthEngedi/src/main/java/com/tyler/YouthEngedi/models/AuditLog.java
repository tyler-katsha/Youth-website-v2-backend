package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.AuditStatus;
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
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long auditLogId;
    @Column(columnDefinition = "TEXT")
    private String value;
    private LocalDateTime now;
    @Enumerated(EnumType.STRING)
    private AuditStatus status;
    private String performedBy;

}
