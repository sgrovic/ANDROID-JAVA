package com.app.cutoff.utils;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositiveAmount(String rawAmount) {
        if (!isNonEmpty(rawAmount)) return false;
        try {
            return Double.parseDouble(rawAmount) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
