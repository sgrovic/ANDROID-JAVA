package com.app.cutoff.utils;

import androidx.appcompat.app.AppCompatDelegate;

import com.app.cutoff.data.preference.ThemePreference;

/**
 * Applies the day/night portion of the theme setting. SAGE and SLATE are
 * palette overlays applied at the Activity level (see BaseActivity /
 * MainActivity setTheme calls) since they aren't tied to day/night.
 */
public final class ThemeManager {

    private ThemeManager() {
    }

    public static void applyNightMode(String themeMode) {
        switch (themeMode) {
            case ThemePreference.MODE_LIGHT:
            case ThemePreference.MODE_YELLOW:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case ThemePreference.MODE_DARK:
            case ThemePreference.MODE_SAGE:
            case ThemePreference.MODE_SLATE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case ThemePreference.MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /** Resolves which style resource to apply on top of night mode, for Sage/Slate. */
    public static int resolveThemeStyleRes(String themeMode) {
        switch (themeMode) {
            case ThemePreference.MODE_YELLOW:
                return com.app.cutoff.R.style.Theme_Cutoff_Yellow;
            case ThemePreference.MODE_SAGE:
                return com.app.cutoff.R.style.Theme_Cutoff_Sage;
            case ThemePreference.MODE_SLATE:
                return com.app.cutoff.R.style.Theme_Cutoff_Slate;
            case ThemePreference.MODE_DARK:
                return com.app.cutoff.R.style.Theme_Cutoff_Dark;
            case ThemePreference.MODE_LIGHT:
            case ThemePreference.MODE_SYSTEM:
            default:
                return com.app.cutoff.R.style.Theme_Cutoff;
        }
    }
}
