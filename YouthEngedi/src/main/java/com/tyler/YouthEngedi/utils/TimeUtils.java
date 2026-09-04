package com.tyler.YouthEngedi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {

    private final static Pattern RELATIVE_DURATION_PATTERN = Pattern.compile("^(\\d+)\\s*(minute|minutes|hour|hours|day|days|week|weeks|month|months)$", Pattern.CASE_INSENSITIVE);

    private TimeUtils(){}
    public static String formatDateTime(LocalDateTime expiresAt){

        if(expiresAt == null) return "Never";

        return expiresAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm"));
    }

    /**
     * Converts an ISO string (e.g. "2026-09-06T12:00" or "2026-09-06")
     * or a relative duration string (e.g. "2 days", "1 week", "12 hours")
     * into a LocalDateTime.
     */
    public static LocalDateTime convertToDateTime(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        String trimmed = date.trim();
        Matcher matcher = RELATIVE_DURATION_PATTERN.matcher(trimmed);

        if (matcher.matches()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            LocalDateTime now = LocalDateTime.now();

            return switch (unit) {
                case "minute", "minutes" -> now.plusMinutes(amount);
                case "hour", "hours" -> now.plusHours(amount);
                case "day", "days" -> now.plusDays(amount);
                case "week", "weeks" -> now.plusWeeks(amount);
                case "month", "months" -> now.plusMonths(amount);
                default -> now.plusDays(1);
            };
        }

        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(trimmed).atStartOfDay();
        }
    }

    public static LocalDateTime getExpiresAt(LocalDate eventDate){
        return LocalDateTime.of(eventDate,LocalTime.MIDNIGHT);
    }

}
