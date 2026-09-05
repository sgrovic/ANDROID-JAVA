package com.app.cutoff.utils;

public final class Constants {

    private Constants() {
    }

    // Nav argument keys
    public static final String ARG_CUTOFF_ID = "arg_cutoff_id";
    public static final String ARG_BILL_ID = "arg_bill_id";

    // Banks / wallets available for bill payments.
    public static final String DEFAULT_BILL_BANK = "BDO";
    public static final String[] BILL_BANKS = {
            "UNIONBANK", "BDO", "MARIBANK", "METROBANK", "GOTYME", "GCASH", "MAYA"
    };

    // Icon keys used for fixed bills (map to drawables in the UI layer)
    public static final String ICON_GROCERY = "grocery";
    public static final String ICON_PARENT = "parent";
    public static final String ICON_PARKING = "parking";
    public static final String ICON_EGG = "egg";
    public static final String ICON_GAS = "gas";
    public static final String ICON_MOTOR_WASH = "motor_wash";
    public static final String ICON_DEFAULT = "default";
}
