package com.app.cutoff.domain.model;

/**
 * Plain domain representation of a bill-like row, decoupled from the Room
 * entity so ViewModels/UI never depend on persistence annotations.
 * Mirrors BillEntity's type/isTemplate/cutoffId fields.
 */
public class Bill {

    public enum Type {
        FIXED, VARIABLE, INCENTIVE
    }

    private final long id;
    private final Type type;
    private final String name;
    private final double amount;
    private final String bank;
    private final String iconKey;
    private final boolean template;
    private final Long cutoffId;
    private final Long sourceBillId;
    private final boolean active;
    private final boolean paid;
    private final int sortOrder;

    public Bill(long id, Type type, String name, double amount, String iconKey, boolean template,
                Long cutoffId, Long sourceBillId, boolean active, boolean paid, int sortOrder) {
        this(id, type, name, amount, iconKey, template, cutoffId, sourceBillId, active, paid, sortOrder,
                com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK);
    }

    public Bill(long id, Type type, String name, double amount, String iconKey, boolean template,
                Long cutoffId, Long sourceBillId, boolean active, boolean paid, int sortOrder, String bank) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.bank = (bank == null || bank.trim().isEmpty())
                ? com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK : bank;
        this.iconKey = iconKey;
        this.template = template;
        this.cutoffId = cutoffId;
        this.sourceBillId = sourceBillId;
        this.active = active;
        this.paid = paid;
        this.sortOrder = sortOrder;
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getBank() {
        return bank;
    }

    public String getIconKey() {
        return iconKey;
    }

    public boolean isTemplate() {
        return template;
    }

    public Long getCutoffId() {
        return cutoffId;
    }

    public Long getSourceBillId() {
        return sourceBillId;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaid() {
        return paid;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
