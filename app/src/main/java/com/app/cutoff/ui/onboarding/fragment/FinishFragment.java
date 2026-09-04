package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.app.cutoff.R;
import com.app.cutoff.ui.onboarding.activity.OnboardingActivity;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 5: All set. "Go to home" triggers OnboardingViewModel to persist
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

        viewModel.getOnboardingComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (Boolean.TRUE.equals(isComplete) && getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).navigateToMainActivity();
            }
        });

        view.findViewById(R.id.button_go_to_home).setOnClickListener(v ->
                viewModel.completeOnboarding());
    }
}
