package com.app.cutoff.ui.home.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.app.cutoff.R;
import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.ui.onboarding.activity.OnboardingActivity;
import com.app.cutoff.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single Activity hosting the post-onboarding app: a NavHostFragment with
 * Home and Settings as top-level (bottom nav) destinations, and Cutoff
 * Detail / Fixed Bills management pushed on top as regular destinations.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    ThemePreference themePreference;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hilt injects @Inject fields for @AndroidEntryPoint activities inside
        // super.onCreate(), so themePreference is still null before this call —
        // setTheme() must come after super.onCreate() (it only needs to run
        // before setContentView(), which is still satisfied here).
        super.onCreate(savedInstanceState);
        setTheme(ThemeManager.resolveThemeStyleRes(themePreferenceModeOrDefault()));

        if (!isOnboardingComplete()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        FragmentContainerView navHostContainer = findViewById(R.id.nav_host_fragment);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(navHostContainer.getId());
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    private String themePreferenceModeOrDefault() {
        return themePreference != null ? themePreference.getThemeMode() : ThemePreference.MODE_SYSTEM;
    }

    private boolean isOnboardingComplete() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("onboarding_complete", false);
    }
}
