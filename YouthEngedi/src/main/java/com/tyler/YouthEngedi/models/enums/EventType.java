package com.tyler.YouthEngedi.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum EventType {

    GENERAL("#2563eb"),
    MEETING("#10b981"),
    WORSHIP("#8b5cf6"),
    URGENT("#ef4444"),
    ACTIVITY("#f59e0b");

    String value;

    public static EventType fromLegacyEventTypeToNewEventType(EventType legacyEventType){
        System.out.println("EventType being received: " + legacyEventType.toString().toUpperCase());
        return switch(legacyEventType.toString().toUpperCase()){
            case "GREEN"  -> MEETING;
            case "ORANGE" -> ACTIVITY;
            case "RED"    -> URGENT;
            case "PURPLE" -> WORSHIP;
            default       -> GENERAL;
        };
    }
}
