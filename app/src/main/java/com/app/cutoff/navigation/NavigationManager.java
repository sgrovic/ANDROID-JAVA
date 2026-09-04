package com.app.cutoff.navigation;

import android.os.Bundle;

import androidx.navigation.NavController;

import com.app.cutoff.R;
import com.app.cutoff.utils.Constants;

/**
 * Thin wrapper around NavController so fragments call named methods
 * (e.g. toCutoffDetail) instead of scattering R.id.action_* references
 * and Bundle-building throughout the UI layer.
 */
public class NavigationManager {

    private final NavController navController;

    public NavigationManager(NavController navController) {
        this.navController = navController;
    }

    public void toCutoffDetail(long cutoffId) {
        Bundle args = new Bundle();
        args.putLong(Constants.ARG_CUTOFF_ID, cutoffId);
        navController.navigate(R.id.action_home_to_cutoffDetail, args);
    }

    public void toFixedBillsManagement() {
        navController.navigate(R.id.action_settings_to_fixedBills);
    }

    public void back() {
        navController.popBackStack();
    }

}
