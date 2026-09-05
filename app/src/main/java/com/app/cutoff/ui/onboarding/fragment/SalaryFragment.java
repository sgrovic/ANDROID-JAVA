package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;
import com.app.cutoff.utils.ValidationUtils;

import dagger.hilt.android.AndroidEntryPoint;

/** Screen 3: Set salary (1st-15th and 16th-end amounts). */
@AndroidEntryPoint
public class SalaryFragment extends Fragment {

    private OnboardingViewModel viewModel;

    public SalaryFragment() {
        super(R.layout.fragment_salary);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scoped to OnboardingActivity (not this Fragment) so the same instance,
        // and its accumulated OnboardingState, is shared across all 6 onboarding screens.
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        EditText inputFirstHalf = view.findViewById(R.id.input_first_to_fifteenth);
        EditText inputSecondHalf = view.findViewById(R.id.input_sixteenth_to_end);

        if (viewModel.getState().isImported()) {
            view.findViewById(R.id.text_import_badge).setVisibility(View.VISIBLE);
            inputFirstHalf.setText(formatPrefill(viewModel.getState().getFirstToFifteenth()));
            inputSecondHalf.setText(formatPrefill(viewModel.getState().getSixteenthToEnd()));
        }

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        view.findViewById(R.id.button_next).setOnClickListener(v -> {
            String rawFirst = inputFirstHalf.getText().toString();
            String rawSecond = inputSecondHalf.getText().toString();

            if (!ValidationUtils.isPositiveAmount(rawFirst) || !ValidationUtils.isPositiveAmount(rawSecond)) {
                Toast.makeText(requireContext(), "Enter valid amounts for both periods", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.setSalary(Double.parseDouble(rawFirst), Double.parseDouble(rawSecond));
            NavHostFragment.findNavController(this).navigate(R.id.action_salary_to_theme);
        });
    }

    /** Drops the ".0" for whole-number amounts so a prefilled 15000 doesn't show as "15000.0". */
    private String formatPrefill(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
