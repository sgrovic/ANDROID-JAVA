package com.app.cutoff.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Stores the user's chosen theme. Default is SYSTEM (follow day/night).
 * SAGE and SLATE are palette overrides independent of day/night and are
 * applied via a themed Activity base, not AppCompatDelegate night mode.
 */
@Singleton
public class ThemePreference {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final String MODE_SYSTEM = "SYSTEM";
    public static final String MODE_LIGHT = "LIGHT";
    public static final String MODE_DARK = "DARK";
    public static final String MODE_SAGE = "SAGE";
    public static final String MODE_SLATE = "SLATE";
    public static final String MODE_YELLOW = "YELLOW";

    private final SharedPreferences prefs;

    @Inject
    public ThemePreference(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }
}
