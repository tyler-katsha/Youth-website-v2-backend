package com.tyler.YouthEngedi.models.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder
public class ContinueAsGuestEvent extends BaseAuthEvent{}
