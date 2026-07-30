package com.tyler.YouthEngedi.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeUtils {
    public static String formatTimeRemaining(long totalSeconds){
        if(totalSeconds <= 0){
            return "Expired";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        List<String> parts = new ArrayList<>();

        if(days > 0){
            parts.add(days + "d");
        }

        if(hours > 0){
            parts.add(hours + "hrs");
        }

        if(minutes > 0){
            parts.add(minutes + "m");
        }

        if(parts.isEmpty()){
            return " < 1m";
        }

        return String.join(" ", parts);
    }

    public static String getRemainingTimeText(LocalDateTime expiresAt){

        if(expiresAt == null) return "Never";

        long secondsRemaining = Duration.between(LocalDateTime.now(),expiresAt).getSeconds();
        return formatTimeRemaining(secondsRemaining);
    }

    public static LocalDateTime convertToDateTime(String date){
        return LocalDateTime.parse(date);
    }

    public static LocalDateTime getExpiresAt(LocalDate eventDate){
        return LocalDateTime.of(eventDate,LocalTime.MIDNIGHT);
    }
}
