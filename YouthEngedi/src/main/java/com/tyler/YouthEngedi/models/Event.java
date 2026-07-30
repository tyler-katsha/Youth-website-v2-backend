package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long eventId;
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private long createdByUserId;
    private LocalDate eventDate;
}
