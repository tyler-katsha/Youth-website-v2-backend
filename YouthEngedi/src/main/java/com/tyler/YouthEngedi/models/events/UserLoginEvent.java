package com.tyler.YouthEngedi.models.events;


import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder
public final class UserLoginEvent extends BaseAuthEvent{}
