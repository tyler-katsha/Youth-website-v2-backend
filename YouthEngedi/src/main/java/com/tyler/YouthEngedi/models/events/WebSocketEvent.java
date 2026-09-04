package com.tyler.YouthEngedi.models.events;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder
public class WebSocketEvent extends BaseAuthEvent{
    private ConnectionType connectionType;
}
