package com.app.cutoff.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * General app-level flags that don't belong in Room (small, non-relational,
 * needed before the DB is even queried).
 */
@Singleton
public class AppPreference {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final SharedPreferences prefs;

    @Inject
    public AppPreference(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingComplete() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public void setOnboardingComplete(boolean complete) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
    }
}
