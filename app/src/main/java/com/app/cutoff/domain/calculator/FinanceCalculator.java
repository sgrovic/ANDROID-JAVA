package com.app.cutoff.domain.calculator;

import com.app.cutoff.domain.model.Bill;

import java.util.List;

/**
 * Pure, side-effect-free calculations over a cutoff's bills. Kept separate
 * from repositories/use cases so it's trivially unit-testable (no Room,
 * no Android framework dependencies).
 */
public final class FinanceCalculator {

    private FinanceCalculator() {
    }

    /** Sum of FIXED + VARIABLE bills — i.e. total expenses for a cutoff. */
    public static double totalExpenses(List<Bill> bills) {
        double total = 0;
        for (Bill bill : bills) {
            if (bill.getType() == Bill.Type.FIXED || bill.getType() == Bill.Type.VARIABLE) {
                total += bill.getAmount();
            }
        }
        return total;
    }

    /** Sum of INCENTIVE rows for a cutoff. */
    public static double totalIncentives(List<Bill> bills) {
        double total = 0;
        for (Bill bill : bills) {
            if (bill.getType() == Bill.Type.INCENTIVE) {
                total += bill.getAmount();
            }
        }
        return total;
    }

    /** salarySnapshot + incentives - expenses. */
    public static double remainingBalance(double salarySnapshot, List<Bill> bills) {
        return salarySnapshot + totalIncentives(bills) - totalExpenses(bills);
    }

    /** Percentage (0-100) of (salary + incentives) that's been spent. */
    public static int spendingProgressPercent(double salarySnapshot, List<Bill> bills) {
        double totalAvailable = salarySnapshot + totalIncentives(bills);
        if (totalAvailable <= 0) return 0;
        double percent = (totalExpenses(bills) / totalAvailable) * 100;
        return (int) Math.min(100, Math.max(0, Math.round(percent)));
    }
}
