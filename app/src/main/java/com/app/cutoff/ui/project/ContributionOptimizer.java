package com.app.cutoff.ui.project;

/** Pure calculation used by the contribution optimization preview and apply flow. */
public final class ContributionOptimizer {
    private ContributionOptimizer() { }

    /**
     * Returns the percentage of total contributions that should be assigned to
     * the first cutoff to minimize the difference between the two balances.
     */
    public static int recommendedFirstPercent(double availableFirst,
                                              double availableSecond,
                                              double contributionTotal) {
        if (contributionTotal <= 0) return 50;
        double idealFirst = (availableFirst - availableSecond + contributionTotal) / 2.0;
        idealFirst = Math.max(0, Math.min(contributionTotal, idealFirst));
        return Math.max(0, Math.min(100,
                (int) Math.round(idealFirst / contributionTotal * 100)));
    }
}
