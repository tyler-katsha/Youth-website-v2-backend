package com.tyler.YouthEngedi.models.dtos;

import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnouncementDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String message;
    @Enumerated(EnumType.STRING)
    private AnnouncementType type;
    private String createdAt;
    private String expiresAt;
    private Boolean isUrgent;
}
