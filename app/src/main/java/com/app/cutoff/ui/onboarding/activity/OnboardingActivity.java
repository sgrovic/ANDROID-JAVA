package com.app.cutoff.ui.onboarding.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.cutoff.R;
import com.app.cutoff.ui.home.activity.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Hosts the 5-screen onboarding flow (Welcome -> Salary -> Theme ->
 * Fixed bills -> All set) as a single nested nav graph. Finishes itself
 * and starts MainActivity once OnboardingViewModel reports completion
 * (see FinishFragment observing getOnboardingComplete()).
 */
@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
    }

    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
