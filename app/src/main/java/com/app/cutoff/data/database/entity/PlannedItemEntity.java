package com.app.cutoff.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** A future purchase deducted from one or both cutoff projections. */
@Entity(tableName = "planned_item",
        foreignKeys = @ForeignKey(entity = BudgetProjectEntity.class,
                parentColumns = "id", childColumns = "projectId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("projectId"))
public class PlannedItemEntity {
    @PrimaryKey(autoGenerate = true) private long id;
    private String name;
    private double totalAmount;
    private int monthsToPay;
    private boolean applyFirstCutoff;
    private boolean applySecondCutoff;
    private long projectId;
    private int firstCutoffPercent;

    public PlannedItemEntity(String name, double totalAmount, int monthsToPay,
                             boolean applyFirstCutoff, boolean applySecondCutoff,
                             long projectId, int firstCutoffPercent) {
        this.name = name; this.totalAmount = totalAmount; this.monthsToPay = monthsToPay;
        this.applyFirstCutoff = applyFirstCutoff; this.applySecondCutoff = applySecondCutoff;
        this.projectId = projectId;
        this.firstCutoffPercent = firstCutoffPercent;
    }
    public long getId() { return id; } public void setId(long id) { this.id = id; }
    public String getName() { return name; } public double getTotalAmount() { return totalAmount; }
    public void setName(String name) { this.name = name; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getMonthsToPay() { return monthsToPay; }
    public boolean isApplyFirstCutoff() { return applyFirstCutoff; }
    public boolean isApplySecondCutoff() { return applySecondCutoff; }
    public void setApplyFirstCutoff(boolean applyFirstCutoff) { this.applyFirstCutoff = applyFirstCutoff; }
    public void setApplySecondCutoff(boolean applySecondCutoff) { this.applySecondCutoff = applySecondCutoff; }
    public long getProjectId() { return projectId; }
    public int getFirstCutoffPercent() { return firstCutoffPercent; }
    public void setFirstCutoffPercent(int firstCutoffPercent) {
        this.firstCutoffPercent = firstCutoffPercent;
    }
}
