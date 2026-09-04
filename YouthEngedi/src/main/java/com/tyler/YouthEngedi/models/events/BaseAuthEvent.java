package com.tyler.YouthEngedi.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class BaseAuthEvent {
    private final String email;
    private final Long userId;
    private final String message;
    @JsonProperty("timestamp")
    private final long timeStamp;
}
