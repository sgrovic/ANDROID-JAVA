package com.app.cutoff.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Single-row table holding the user's configured salary per half-month
 * period. Only one row ever exists (id = SINGLETON_ID); it's updated in
 * place from onboarding or Settings > Salary.
 */
@Entity(tableName = "salary")
public class SalaryEntity {

    public static final String SINGLETON_ID = "salary_singleton";

    @PrimaryKey
    @NonNull
    private String id = SINGLETON_ID;

    private double firstToFifteenth;
    private double sixteenthToEnd;

    public SalaryEntity(double firstToFifteenth, double sixteenthToEnd) {
        this.firstToFifteenth = firstToFifteenth;
        this.sixteenthToEnd = sixteenthToEnd;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public double getFirstToFifteenth() {
        return firstToFifteenth;
    }

    public void setFirstToFifteenth(double firstToFifteenth) {
        this.firstToFifteenth = firstToFifteenth;
    }

    public double getSixteenthToEnd() {
        return sixteenthToEnd;
    }

    public void setSixteenthToEnd(double sixteenthToEnd) {
        this.sixteenthToEnd = sixteenthToEnd;
    }
}
