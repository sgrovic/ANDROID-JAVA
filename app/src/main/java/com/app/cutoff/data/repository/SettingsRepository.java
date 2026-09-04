package com.app.cutoff.data.repository;

import com.app.cutoff.data.preference.AppPreference;
import com.app.cutoff.data.preference.ThemePreference;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Thin facade over ThemePreference + AppPreference so ViewModels depend on
 * one repository instead of reaching into SharedPreferences wrappers
 * directly.
 */
@Singleton
public class SettingsRepository {

    private final ThemePreference themePreference;
    private final AppPreference appPreference;

    @Inject
    public SettingsRepository(ThemePreference themePreference, AppPreference appPreference) {
        this.themePreference = themePreference;
        this.appPreference = appPreference;
    }

    public String getThemeMode() {
        return themePreference.getThemeMode();
    }

    public void setThemeMode(String mode) {
        themePreference.setThemeMode(mode);
    }

    public boolean isOnboardingComplete() {
        return appPreference.isOnboardingComplete();
    }

    public void setOnboardingComplete(boolean complete) {
        appPreference.setOnboardingComplete(complete);
    }
}
