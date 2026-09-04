package com.app.cutoff.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Determines the 1st-15th / 16th-end-of-month cutoff period that a given
 * date falls into. Central place for this rule so it's never duplicated.
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static final int SPLIT_DAY = 15;

    public static LocalDate periodStart(LocalDate date) {
        if (date.getDayOfMonth() <= SPLIT_DAY) {
            return date.withDayOfMonth(1);
        }
        return date.withDayOfMonth(SPLIT_DAY + 1);
    }

    public static LocalDate periodEnd(LocalDate date) {
        if (date.getDayOfMonth() <= SPLIT_DAY) {
            return date.withDayOfMonth(SPLIT_DAY);
        }
        YearMonth yearMonth = YearMonth.from(date);
        return date.withDayOfMonth(yearMonth.lengthOfMonth());
    }

    /** True if `date` falls in the first-half period (1st-15th). */
    public static boolean isFirstHalf(LocalDate date) {
        return date.getDayOfMonth() <= SPLIT_DAY;
    }

    /**
     * The period-end date immediately following the given one. If
     * `periodEnd` is the 15th, the next one is the last day of the same
     * month; if it's the last day of the month, the next one is the 15th
     * of the following month.
     */
    public static LocalDate nextPeriodEnd(LocalDate periodEnd) {
        if (periodEnd.getDayOfMonth() == SPLIT_DAY) {
            return YearMonth.from(periodEnd).atEndOfMonth();
        }
        return periodEnd.plusMonths(1).withDayOfMonth(SPLIT_DAY);
    }

    /**
     * All period-end dates after `afterPeriodEnd`, through December 31 of
     * that same date's year. Used to show the rest of the year's cutoffs
     * on Home before they exist as CutoffEntity rows (those are only
     * created when their period actually starts).
     */
    public static List<LocalDate> upcomingPeriodEndsThroughYearEnd(LocalDate afterPeriodEnd) {
        List<LocalDate> result = new ArrayList<>();
        int year = afterPeriodEnd.getYear();
        LocalDate cursor = afterPeriodEnd;
        while (true) {
            LocalDate next = nextPeriodEnd(cursor);
            if (next.getYear() != year) break;
            result.add(next);
            cursor = next;
        }
        return result;
    }
}
