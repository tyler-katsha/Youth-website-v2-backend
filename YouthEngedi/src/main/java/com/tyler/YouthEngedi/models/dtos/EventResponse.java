package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private String dateKey;
    private String eventType;
    private String color;
}
