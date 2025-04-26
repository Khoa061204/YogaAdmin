package com.example.yogaadmin.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class InputValidator {
    // Valid days of the week
    private static final List<String> VALID_DAYS = Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );

    // Valid class types
    private static final List<String> VALID_CLASS_TYPES = Arrays.asList(
            "Flow Yoga", "Aerial Yoga", "Family Yoga"
    );

    /**
     * Validate day of week
     * @param day Day to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDayOfWeek(String day) {
        return day != null && VALID_DAYS.contains(day);
    }

    /**
     * Validate time format (HH:MM)
     * @param time Time to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTimeFormat(String time) {
        if (time == null || time.isEmpty()) {
            return false;
        }

        // Regex for HH:MM format (24-hour clock)
        return Pattern.matches("^([01]\\d|2[0-3]):([0-5]\\d)$", time);
    }

    /**
     * Validate class capacity
     * @param capacity Number of participants
     * @return true if valid, false otherwise
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0 && capacity <= 50;
    }

    /**
     * Validate class duration
     * @param duration Class duration in minutes
     * @return true if valid, false otherwise
     */
    public static boolean isValidDuration(int duration) {
        return duration > 0 && duration <= 180;
    }

    /**
     * Validate class price
     * @param price Price per class
     * @return true if valid, false otherwise
     */
    public static boolean isValidPrice(double price) {
        return price >= 0 && price <= 100;
    }

    /**
     * Validate class type
     * @param classType Type of yoga class
     * @return true if valid, false otherwise
     */
    public static boolean isValidClassType(String classType) {
        return classType != null && VALID_CLASS_TYPES.contains(classType);
    }
}