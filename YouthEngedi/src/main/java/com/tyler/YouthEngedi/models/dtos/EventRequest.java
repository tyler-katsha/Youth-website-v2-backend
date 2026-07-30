package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {

    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private String dateKey;
    private String eventType;
}
