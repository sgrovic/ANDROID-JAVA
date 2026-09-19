package com.app.cutoff.data.database.entity;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Unified table for every "bill-like" row in the app: fixed bill templates,
 * per-cutoff fixed bill snapshots, variable (one-off) bills, and incentives.
 * The {@link #type} + {@link #isTemplate} combination decides how a row
 * behaves and where it shows up:
 *
 *  - type=FIXED,     isTemplate=true,  cutoffId=null   -> the recurring template,
 *                                                          managed in Settings > Fixed bills.
 *  - type=FIXED,     isTemplate=false, cutoffId=<id>   -> a snapshot copied into a
 *                                                          specific cutoff when it was generated.
 *  - type=VARIABLE,  isTemplate=false, cutoffId=<id>   -> a one-off bill entered
 *                                                          directly within a cutoff.
 *  - type=INCENTIVE, isTemplate=false, cutoffId=<id>   -> extra income added to a
 *                                                          cutoff's salary (e.g. Retro, Bonus).
 *
 * sourceBillId links a FIXED snapshot back to the template it was copied
 * from, for traceability only — editing the template never rewrites the
 * snapshot, and editing the snapshot never rewrites the template.
 */
@Entity(
        tableName = "bill",
        foreignKeys = @ForeignKey(
                entity = CutoffEntity.class,
                parentColumns = "id",
                childColumns = "cutoffId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("cutoffId"), @Index("type")}
)
public class BillEntity {

    public static final String TYPE_FIXED = "FIXED";
    public static final String TYPE_VARIABLE = "VARIABLE";
    public static final String TYPE_INCENTIVE = "INCENTIVE";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String RECURRENCE_FIRST_CUTOFF = "FIRST_CUTOFF";
    public static final String RECURRENCE_SECOND_CUTOFF = "SECOND_CUTOFF";
    public static final String RECURRENCE_BOTH_CUTOFFS = "BOTH_CUTOFFS";

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(defaultValue = "FIXED")
    private String type; // FIXED | VARIABLE | INCENTIVE

    private String name;
    private double amount;

    // Bank / wallet used to pay this bill. Existing and newly created bills default to BDO.
    @Nullable
    private String bank;

    @Nullable
    private String iconKey; // e.g. "grocery", "parking" — null for variable/incentive rows

    // true only for the recurring fixed-bill template row (shown in Settings > Fixed bills).
    private boolean isTemplate;

    // Null for templates. Set for any row that belongs to a specific cutoff
    // (fixed snapshot, variable bill, or incentive).
    @Nullable
    private Long cutoffId;

    // For FIXED snapshots only: points back at the template row it was copied from.
    @Nullable
    private Long sourceBillId;

    private boolean active;   // templates: whether it's still copied into new cutoffs
    private boolean paid;     // snapshots/variable: whether the user marked it paid
    @ColumnInfo(defaultValue = "PENDING")
    private String paymentStatus = PAYMENT_STATUS_PENDING;
    private int sortOrder;

    /** Which salary cutoff receives this fixed-bill template. Templates from
     * earlier app versions are treated as every cutoff. */
    @NonNull
    @ColumnInfo(defaultValue = "BOTH_CUTOFFS")
    private String recurrenceSchedule = RECURRENCE_BOTH_CUTOFFS;

    @androidx.room.Ignore
    public BillEntity(String type, String name, double amount, @Nullable String iconKey,
                       boolean isTemplate, @Nullable Long cutoffId, @Nullable Long sourceBillId,
                       boolean active, boolean paid, int sortOrder) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.iconKey = iconKey;
        this.isTemplate = isTemplate;
        this.cutoffId = cutoffId;
        this.sourceBillId = sourceBillId;
        this.active = active;
        this.paid = paid;
        this.paymentStatus = paid ? PAYMENT_STATUS_PAID : PAYMENT_STATUS_PENDING;
        this.sortOrder = sortOrder;
        this.bank = com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK;
    }

    public BillEntity(String type, String name, double amount, @Nullable String iconKey,
                       boolean isTemplate, @Nullable Long cutoffId, @Nullable Long sourceBillId,
                       boolean active, boolean paid, int sortOrder, String bank) {
        this(type, name, amount, iconKey, isTemplate, cutoffId, sourceBillId, active, paid, sortOrder);
        this.bank = (bank == null || bank.trim().isEmpty())
                ? com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK : bank;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = (bank == null || bank.trim().isEmpty())
                ? com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK : bank;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Nullable
    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(@Nullable String iconKey) {
        this.iconKey = iconKey;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    @Nullable
    public Long getCutoffId() {
        return cutoffId;
    }

    public void setCutoffId(@Nullable Long cutoffId) {
        this.cutoffId = cutoffId;
    }

    @Nullable
    public Long getSourceBillId() {
        return sourceBillId;
    }

    public void setSourceBillId(@Nullable Long sourceBillId) {
        this.sourceBillId = sourceBillId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPaid() {
        return PAYMENT_STATUS_PAID.equals(paymentStatus);
    }

    public String getPaymentStatus() { return paymentStatus == null ? PAYMENT_STATUS_PENDING : paymentStatus; }

    public void setPaymentStatus(String status) {
        boolean isPaid = PAYMENT_STATUS_PAID.equalsIgnoreCase(status);
        paymentStatus = isPaid ? PAYMENT_STATUS_PAID : PAYMENT_STATUS_PENDING;
        paid = isPaid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getRecurrenceSchedule() {
        return recurrenceSchedule;
    }

    public void setRecurrenceSchedule(String recurrenceSchedule) {
        this.recurrenceSchedule = RECURRENCE_FIRST_CUTOFF.equals(recurrenceSchedule)
                || RECURRENCE_SECOND_CUTOFF.equals(recurrenceSchedule)
                || RECURRENCE_BOTH_CUTOFFS.equals(recurrenceSchedule)
                ? recurrenceSchedule : RECURRENCE_BOTH_CUTOFFS;
    }
}
