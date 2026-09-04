package com.tyler.YouthEngedi.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Traffic {
    @JsonProperty("event")
    private WebSocketEvent webSocketEvent;
    @JsonProperty("currentSize")
    private long size;
    @JsonProperty("maxSize")
    private int maxEvents;
}
