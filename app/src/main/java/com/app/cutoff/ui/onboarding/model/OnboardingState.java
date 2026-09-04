package com.app.cutoff.ui.onboarding.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries the user's in-progress selections across the 5 onboarding
 * screens (Welcome -> Salary -> Theme -> Fixed bills -> Finish) before
 * anything is committed to the database on the final step.
 */
public class OnboardingState {

    public static class DraftFixedBill {
        public String name;
        public double amount;
        public String iconKey;

        public DraftFixedBill(String name, double amount, String iconKey) {
            this.name = name;
            this.amount = amount;
            this.iconKey = iconKey;
        }
    }

    private double firstToFifteenth;
    private double sixteenthToEnd;
    private String themeMode;
    private final List<DraftFixedBill> draftFixedBills = new ArrayList<>();

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

    public String getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode;
    }

    public List<DraftFixedBill> getDraftFixedBills() {
        return draftFixedBills;
    }

    public void addDraftFixedBill(DraftFixedBill bill) {
        draftFixedBills.add(bill);
    }

    public void removeDraftFixedBill(DraftFixedBill bill) {
        draftFixedBills.remove(bill);
    }
}
