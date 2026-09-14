package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.ui.onboarding.adapter.ThemeAdapter;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;
import com.app.cutoff.utils.ThemeManager;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 4: Choose theme. Selecting an option applies it immediately
 * (ThemeManager.applyNightMode + Activity.recreate()), the same live-
 * preview pattern SettingsFragment uses post-onboarding, rather than only
 * taking effect once onboarding finishes.
 */
@AndroidEntryPoint
public class ThemeFragment extends Fragment {

    private OnboardingViewModel viewModel;

    public ThemeFragment() {
        super(R.layout.fragment_theme);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        List<ThemeAdapter.ThemeOption> options = Arrays.asList(
                new ThemeAdapter.ThemeOption(ThemePreference.MODE_LIGHT, "Light", R.drawable.preview_theme_light),
                new ThemeAdapter.ThemeOption(ThemePreference.MODE_DARK, "Dark", R.drawable.preview_theme_dark),
                new ThemeAdapter.ThemeOption(ThemePreference.MODE_SAGE, "Sage", R.drawable.preview_theme_sage),
                new ThemeAdapter.ThemeOption(ThemePreference.MODE_SLATE, "Slate", R.drawable.preview_theme_slate),
                new ThemeAdapter.ThemeOption(ThemePreference.MODE_YELLOW, "Yellow", R.drawable.preview_theme_yellow)
        );

        ThemeAdapter adapter = new ThemeAdapter(options, viewModel.getState().getThemeMode(), mode -> {
            boolean changed = viewModel.setThemeMode(mode);
            if (changed) {
                // Must happen before recreate(): setTheme() alone isn't enough to
                // pick up Sage/Slate's overlay style, and OnboardingActivity reads
                // the (now-persisted) preference back in its onCreate().
                ThemeManager.applyNightMode(mode);
                requireActivity().recreate();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_theme_options);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        view.findViewById(R.id.button_next).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_theme_to_fixedBills));
    }
}
