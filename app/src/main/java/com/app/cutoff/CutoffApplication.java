package com.app.cutoff;

import android.app.Application;

import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.utils.ThemeManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application entry point. Hilt generates the DI component graph rooted
 * here. Applies the saved theme preference before any Activity is created
 * to avoid a flash of the wrong theme.
 */
@HiltAndroidApp
public class CutoffApplication extends Application {

    @Inject
    ThemePreference themePreference;

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyNightMode(themePreference.getThemeMode());
    }
}
