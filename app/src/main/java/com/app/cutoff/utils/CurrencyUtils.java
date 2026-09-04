package com.app.cutoff.utils;

import java.text.DecimalFormat;

/** Formats amounts as peso strings, e.g. 13500.0 -> "₱13,500". */
public final class CurrencyUtils {

    private CurrencyUtils() {
    }

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    public static String format(double amount) {
        return "\u20B1" + FORMAT.format(amount);
    }
}
