package com.app.cutoff.data.database.pojo;

/**
 * Aggregated view of a cutoff for list rows (Home > past cutoffs) that need
 * the remaining amount without loading every bill row individually.
 * Populated directly by a Room @Query (see CutoffDao#observePastCutoffSummaries),
 * so field names must match the query's column aliases exactly.
 */
public class CutoffSummary {

    private long id;
    private long periodEndEpochDay;
    private double salarySnapshot;
    private double totalExpenses;
    private double totalIncentives;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPeriodEndEpochDay() {
        return periodEndEpochDay;
    }

    public void setPeriodEndEpochDay(long periodEndEpochDay) {
        this.periodEndEpochDay = periodEndEpochDay;
    }

    public double getSalarySnapshot() {
        return salarySnapshot;
    }

    public void setSalarySnapshot(double salarySnapshot) {
        this.salarySnapshot = salarySnapshot;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalIncentives() {
        return totalIncentives;
    }

    public void setTotalIncentives(double totalIncentives) {
        this.totalIncentives = totalIncentives;
    }

    /** Salary + incentives - expenses: the "X left" figure shown per row. */
    public double getRemaining() {
        return salarySnapshot + totalIncentives - totalExpenses;
    }
}
