package com.app.cutoff.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A separate savings goal shown on the Project tab. */
@Entity(tableName = "budget_project")
public class BudgetProjectEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private double targetAmount;
    private double savedAmount;
    private long createdAtEpochDay;

    public BudgetProjectEntity(String name, double targetAmount, double savedAmount,
                               long createdAtEpochDay) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.createdAtEpochDay = createdAtEpochDay;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }
    public long getCreatedAtEpochDay() { return createdAtEpochDay; }
}
