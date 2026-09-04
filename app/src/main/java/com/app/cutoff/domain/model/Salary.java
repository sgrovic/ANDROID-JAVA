package com.app.cutoff.domain.model;

/** Plain domain representation of the user's configured salary. */
public class Salary {

    private final double firstToFifteenth;
    private final double sixteenthToEnd;

    public Salary(double firstToFifteenth, double sixteenthToEnd) {
        this.firstToFifteenth = firstToFifteenth;
        this.sixteenthToEnd = sixteenthToEnd;
    }

    public double getFirstToFifteenth() {
        return firstToFifteenth;
    }

    public double getSixteenthToEnd() {
        return sixteenthToEnd;
    }
}
