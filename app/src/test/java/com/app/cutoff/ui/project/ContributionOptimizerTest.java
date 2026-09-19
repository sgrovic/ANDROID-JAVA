package com.app.cutoff.ui.project;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ContributionOptimizerTest {
    @Test public void equalAvailableBalancesSplitsEvenly() {
        assertEquals(50, ContributionOptimizer.recommendedFirstPercent(5000, 5000, 3000));
    }

    @Test public void assignsMoreToCutoffWithMoreAvailableMoney() {
        assertEquals(67, ContributionOptimizer.recommendedFirstPercent(6000, 4000, 6000));
    }

    @Test public void clampsToFirstCutoffWhenPerfectBalanceIsImpossible() {
        assertEquals(100, ContributionOptimizer.recommendedFirstPercent(7000, 1000, 2000));
    }

    @Test public void clampsToSecondCutoffWhenPerfectBalanceIsImpossible() {
        assertEquals(0, ContributionOptimizer.recommendedFirstPercent(1000, 7000, 2000));
    }

    @Test public void noContributionsUsesNeutralSplit() {
        assertEquals(50, ContributionOptimizer.recommendedFirstPercent(5000, 1000, 0));
    }
}
