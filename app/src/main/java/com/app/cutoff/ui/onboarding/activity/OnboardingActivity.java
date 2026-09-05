package com.app.cutoff.ui.onboarding.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.cutoff.R;
import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.ui.home.activity.MainActivity;
import com.app.cutoff.utils.ThemeManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Hosts the 6-screen onboarding flow (Welcome -> Import -> Salary -> Theme
 * -> Fixed bills -> All set) as a single nested nav graph. Finishes itself
 * and starts MainActivity once OnboardingViewModel reports completion
 * (see FinishFragment observing getOnboardingComplete()).
 */
@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {

    @Inject
    ThemePreference themePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Same ordering as MainActivity: Hilt injects fields inside
        // super.onCreate(), so setTheme() has to come after it, but still
        // before setContentView(). This mirrors ThemeFragment's live-preview
        // recreate() -- see OnboardingViewModel.setThemeMode().
        super.onCreate(savedInstanceState);
        setTheme(ThemeManager.resolveThemeStyleRes(themePreference.getThemeMode()));
        setContentView(R.layout.activity_onboarding);
    }

    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
