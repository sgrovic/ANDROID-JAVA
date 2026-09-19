package com.app.cutoff.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.cutoff.R;
import com.google.android.material.color.MaterialColors;

import java.util.Locale;

/** Applies a consistent logo and 30%-wide bank gradient to every bill row. */
public final class BankGradientUtils {

    private static final float GRADIENT_END = 0.70f;

    private BankGradientUtils() {
    }

    public static void apply(View card, TextView name, TextView amount, TextView bank,
                             View actionButton, String bankName) {
        BankStyle style = BankStyle.forName(bankName);
        int surfaceColor = MaterialColors.getColor(
                card, com.google.android.material.R.attr.colorSurface);
        int contentColor = MaterialColors.getColor(
                card, com.google.android.material.R.attr.colorOnSurface);

        card.setBackground(new BankRowDrawable(
                surfaceColor, style.gradientColor, dp(card.getContext(), 16), GRADIENT_END));
        if (name != null) name.setTextColor(contentColor);
        if (amount != null) amount.setTextColor(contentColor);
        if (bank != null) bank.setTextColor(contentColor);

        TextView logo = card.findViewById(R.id.text_bank_logo);
        if (logo != null) {
            logo.setText(style.logoText);
            logo.setTextColor(style.logoTextColor);
            logo.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.logoTextSizeSp);
            logo.setLetterSpacing(style.letterSpacing);

            GradientDrawable logoBackground = new GradientDrawable();
            logoBackground.setColor(style.logoBackgroundColor);
            logoBackground.setCornerRadius(dp(card.getContext(), 12));
            logo.setBackground(logoBackground);
        }

        card.setContentDescription((name == null ? "" : name.getText())
                + (amount == null ? "" : ", " + amount.getText())
                + ", " + normalize(bankName));

        if (actionButton instanceof ImageButton) {
            ((ImageButton) actionButton).setColorFilter(contentColor);
            actionButton.setAlpha(0.72f);
        }
    }

    public static void apply(View card, TextView name, TextView amount, TextView bank,
                             String bankName) {
        apply(card, name, amount, bank, null, bankName);
    }

    private static String normalize(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return Constants.DEFAULT_BILL_BANK;
        }
        return bankName.trim().toUpperCase(Locale.US);
    }

    private static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    private static int withAlpha(@ColorInt int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static final class BankStyle {
        final String logoText;
        final int logoBackgroundColor;
        final int logoTextColor;
        final int gradientColor;
        final float logoTextSizeSp;
        final float letterSpacing;

        BankStyle(String logoText, String logoBackground, String logoTextColor,
                  String gradientColor, float logoTextSizeSp, float letterSpacing) {
            this.logoText = logoText;
            this.logoBackgroundColor = Color.parseColor(logoBackground);
            this.logoTextColor = Color.parseColor(logoTextColor);
            this.gradientColor = Color.parseColor(gradientColor);
            this.logoTextSizeSp = logoTextSizeSp;
            this.letterSpacing = letterSpacing;
        }

        static BankStyle forName(String bankName) {
            switch (normalize(bankName)) {
                case "UNIONBANK":
                    return new BankStyle("U", "#8E4218", "#FFFFFF", "#C65D22", 15, 0);
                case "MARIBANK":
                    return new BankStyle("mari", "#B3262E", "#FFFFFF", "#D93A42", 9, 0);
                case "METROBANK":
                    return new BankStyle("MB", "#123A83", "#FFFFFF", "#246BCE", 11, 0.02f);
                case "GOTYME":
                    return new BankStyle("go\ntyme", "#077B83", "#FFFFFF", "#18A8AD", 8, 0);
                case "GCASH":
                    return new BankStyle("G", "#1458BF", "#FFFFFF", "#2D7DE0", 16, 0);
                case "MAYA":
                    return new BankStyle("maya", "#087B69", "#FFFFFF", "#18A97F", 9, 0);
                case "BDO":
                    return new BankStyle("BDO", "#173F7A", "#FFE29A", "#245CA2", 11, 0.01f);
                default:
                    return customStyle(bankName);
            }
        }

        private static BankStyle customStyle(String bankName) {
            String clean = bankName == null ? "" : bankName.trim().replaceAll("\\s+", " ");
            if (clean.isEmpty()) clean = "BANK";
            String[] words = clean.split(" ");
            StringBuilder abbreviation = new StringBuilder();
            if (words.length > 1) {
                for (String word : words) if (!word.isEmpty()) abbreviation.append(word.charAt(0));
            } else {
                abbreviation.append(clean.length() <= 4 ? clean : clean.substring(0, 3));
            }
            String logo = abbreviation.toString().toUpperCase(Locale.US);
            float size = logo.length() <= 3 ? 12 : (logo.length() <= 5 ? 10 : 8);
            return new BankStyle(logo, "#455A64", "#FFFFFF", "#607D8B", size, 0.01f);
        }
    }

    /** Draws the normal surface first, then clips the bank tint to the rounded row. */
    private static final class BankRowDrawable extends Drawable {
        private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path clipPath = new Path();
        private final RectF rect = new RectF();
        private final int bankColor;
        private final float radius;
        private final float gradientEnd;
        private int drawableAlpha = 255;

        BankRowDrawable(@ColorInt int surfaceColor, @ColorInt int bankColor,
                        float radius, float gradientEnd) {
            basePaint.setColor(surfaceColor);
            this.bankColor = bankColor;
            this.radius = radius;
            this.gradientEnd = gradientEnd;
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            rect.set(bounds);
            clipPath.reset();
            clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
            float gradientWidth = Math.max(1, bounds.width() * gradientEnd);
            gradientPaint.setShader(new LinearGradient(
                    bounds.left, bounds.top, bounds.left + gradientWidth, bounds.top,
                    withAlpha(bankColor, 112), withAlpha(bankColor, 0), Shader.TileMode.CLAMP));
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            basePaint.setAlpha(drawableAlpha);
            gradientPaint.setAlpha(drawableAlpha);
            canvas.drawRoundRect(rect, radius, radius, basePaint);
            int save = canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawRect(rect.left, rect.top,
                    rect.left + rect.width() * gradientEnd, rect.bottom, gradientPaint);
            canvas.restoreToCount(save);
        }

        @Override
        public void setAlpha(int alpha) {
            drawableAlpha = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            basePaint.setColorFilter(colorFilter);
            gradientPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
