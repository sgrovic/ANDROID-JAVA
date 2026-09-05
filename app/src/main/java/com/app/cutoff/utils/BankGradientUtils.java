package com.app.cutoff.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;

/**
 * Provides the visual identity for bill cards based on the payment bank/wallet.
 * No bank logos are used; the bank is represented by a subtle three-stop gradient.
 */
public final class BankGradientUtils {

    private BankGradientUtils() {
    }

    public static void apply(View card, TextView name, TextView amount, TextView bank,
                             View actionButton, String bankName) {
        boolean dark = isDarkTheme(card.getContext());
        int[] colors = getGradientColors(bankName, dark);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        drawable.setCornerRadius(dp(card.getContext(), 12));
        card.setBackground(drawable);

        int textColor = getTextColor(bankName, dark);
        name.setTextColor(textColor);
        amount.setTextColor(textColor);
        bank.setTextColor(textColor);

        if (actionButton instanceof ImageButton) {
            ((ImageButton) actionButton).setColorFilter(textColor);
            actionButton.setAlpha(0.65f);
        }
    }

    public static void apply(View card, TextView name, TextView amount, TextView bank,
                             String bankName) {
        apply(card, name, amount, bank, null, bankName);
    }

    private static int[] getGradientColors(String bankName, boolean dark) {
        String bank = bankName == null ? Constants.DEFAULT_BILL_BANK : bankName.trim().toUpperCase();

        switch (bank) {
            case "UNIONBANK":
                return dark
                        ? colors("#FF6A00", "#7A3B12", "#202020")
                        : colors("#FFE0C2", "#FFF1E5", "#E7E7E7");
            case "METROBANK":
                return dark
                        ? colors("#071B45", "#123A83", "#246BCE")
                        : colors("#DCE8FF", "#BBD3FF", "#E5F0FF");
            case "MARIBANK":
                return dark
                        ? colors("#7A1118", "#C92F36", "#FF6B6B")
                        : colors("#FFE0E0", "#FFD0D0", "#FFF0F0");
            case "GOTYME":
                return dark
                        ? colors("#063D4B", "#007C91", "#20BFC5")
                        : colors("#D9F7FA", "#C8EFF1", "#DFFAF7");
            case "GCASH":
                return dark
                        ? colors("#0639A6", "#1262D8", "#48B8F5")
                        : colors("#DCEBFF", "#C7E0FF", "#E5F7FF");
            case "MAYA":
                return dark
                        ? colors("#073D36", "#087B69", "#36C78F")
                        : colors("#DDF7EE", "#C6EBDD", "#E8F8F1");
            case "BDO":
            default:
                return dark
                        ? colors("#063D8C", "#173F7A", "#B88A13")
                        : colors("#D9EAFF", "#FFFFFF", "#FFF1B8");
        }
    }

    @ColorInt
    private static int getTextColor(String bankName, boolean dark) {
        if (dark) {
            return Color.WHITE;
        }

        String bank = bankName == null ? Constants.DEFAULT_BILL_BANK : bankName.trim().toUpperCase();
        // Dark text is best for the light/pastel gradients. Dark cards use white instead.
        return Color.rgb(25, 28, 32);
    }

    private static int[] colors(String first, String second, String third) {
        return new int[]{Color.parseColor(first), Color.parseColor(second), Color.parseColor(third)};
    }

    private static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    private static boolean isDarkTheme(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
