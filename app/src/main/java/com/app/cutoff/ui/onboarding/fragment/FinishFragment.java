package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.cutoff.R;
import com.app.cutoff.ui.onboarding.activity.OnboardingActivity;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 6: All set. "Go to home" triggers OnboardingViewModel to persist
 * everything (salary, theme, fixed bill templates), then — once
 * getOnboardingComplete() fires true — hands off to OnboardingActivity to
 * start MainActivity.
 */
@AndroidEntryPoint
public class FinishFragment extends Fragment {

    private OnboardingViewModel viewModel;

    public FinishFragment() {
        super(R.layout.fragment_finish);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        com.app.cutoff.ui.onboarding.model.OnboardingState state = viewModel.getState();
        java.time.LocalDate today = java.time.LocalDate.now();
        double income = com.app.cutoff.utils.DateUtils.isFirstHalf(today)
                ? state.getFirstToFifteenth() : state.getSixteenthToEnd();
        double bills = 0;
        for (com.app.cutoff.ui.onboarding.model.OnboardingState.DraftFixedBill bill : state.getDraftFixedBills()) {
            bills += bill.amount;
        }
        double remaining = income - bills;
        String period = com.app.cutoff.utils.DateUtils.periodStart(today)
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
                + "–" + com.app.cutoff.utils.DateUtils.periodEnd(today)
                .format(java.time.format.DateTimeFormatter.ofPattern("d, yyyy"));
        ((TextView) view.findViewById(R.id.text_cutoff_period)).setText(period);
        ((TextView) view.findViewById(R.id.text_salary_value)).setText(com.app.cutoff.utils.CurrencyUtils.format(income));
        ((TextView) view.findViewById(R.id.text_total_bill_value)).setText(com.app.cutoff.utils.CurrencyUtils.format(bills));
        ((TextView) view.findViewById(R.id.text_remaining_value)).setText(com.app.cutoff.utils.CurrencyUtils.format(remaining));
        ((TextView) view.findViewById(R.id.text_summary_remaining)).setText(com.app.cutoff.utils.CurrencyUtils.format(remaining));
        int percent = income > 0 ? (int) Math.round(bills / income * 100) : 0;
        ((ProgressBar) view.findViewById(R.id.progress_spending)).setProgress(Math.min(100, Math.max(0, percent)));
        ((TextView) view.findViewById(R.id.text_spending_percent)).setText(percent + "% of income allocated to bills");

        viewModel.getOnboardingComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (Boolean.TRUE.equals(isComplete) && getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).navigateToMainActivity();
            }
        });

        view.findViewById(R.id.button_go_to_home).setOnClickListener(v ->
                viewModel.completeOnboarding());
    }
}
