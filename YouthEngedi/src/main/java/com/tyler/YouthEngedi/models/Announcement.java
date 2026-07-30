package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long announcementId;
    private String title;
    private String message;
    @Enumerated(EnumType.STRING)
    private AnnouncementType type;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isUrgent;
    @ManyToOne
    @JoinColumn(name="event_id")
    private Event event;
}
