package com.app.cutoff.domain.usecase;

import com.app.cutoff.domain.calculator.FinanceCalculator;
import com.app.cutoff.domain.model.Bill;

import java.util.List;

import javax.inject.Inject;

/**
 * Thin use-case wrapper around FinanceCalculator so ViewModels depend on
 * an injectable use case rather than calling a static utility directly —
 * keeps ViewModels test-friendly (the use case can be mocked).
 */
public class CalculateRemainingBalance {

    @Inject
    public CalculateRemainingBalance() {
    }

    public double execute(double salarySnapshot, List<Bill> bills) {
        return FinanceCalculator.remainingBalance(salarySnapshot, bills);
    }

    public int spendingProgressPercent(double salarySnapshot, List<Bill> bills) {
        return FinanceCalculator.spendingProgressPercent(salarySnapshot, bills);
    }
}
