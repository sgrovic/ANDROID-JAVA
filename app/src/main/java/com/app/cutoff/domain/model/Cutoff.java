package com.app.cutoff.domain.model;

import java.time.LocalDate;

/** Plain domain representation of a half-month cutoff period. */
public class Cutoff {

    private final long id;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final double salarySnapshot;

    public Cutoff(long id, LocalDate periodStart, LocalDate periodEnd, double salarySnapshot) {
        this.id = id;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.salarySnapshot = salarySnapshot;
    }

    public long getId() {
        return id;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public double getSalarySnapshot() {
        return salarySnapshot;
    }

    public boolean containsDay(LocalDate day) {
        return !day.isBefore(periodStart) && !day.isAfter(periodEnd);
    }

    public long daysRemaining(LocalDate today) {
        if (today.isAfter(periodEnd)) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(today, periodEnd);
    }
}
