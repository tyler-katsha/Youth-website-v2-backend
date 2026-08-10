package com.tyler.YouthEngedi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String getRemainingTimeText(LocalDateTime expiresAt){

        if(expiresAt == null) return "Never";

        return expiresAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm"));
    }

    public static LocalDateTime convertToDateTime(String date){
        return LocalDateTime.parse(date);
    }

    public static LocalDateTime getExpiresAt(LocalDate eventDate){
        return LocalDateTime.of(eventDate,LocalTime.MIDNIGHT);
    }
}
