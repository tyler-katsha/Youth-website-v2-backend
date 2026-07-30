package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse {
    private long id;
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private String dateKey;
    private String eventType;
    private String color;
}
