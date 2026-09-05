package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 2: Import your data?. Offers to prefill salary and fixed bills
 * from a Cutoff backup file (see BackupRepository/BackupManager) before
 * the user sets them up manually. Nothing is written to the database
 * here -- a successful parse just fills OnboardingState, which is
 * committed as a whole when onboarding finishes.
 */
@AndroidEntryPoint
public class ImportPromptFragment extends Fragment {

    private OnboardingViewModel viewModel;

    // Registered as a field so it's set up before the Fragment reaches
    // STARTED, per the ActivityResultLauncher contract.
    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) viewModel.importFromFile(uri);
            });

    public ImportPromptFragment() {
        super(R.layout.fragment_import_prompt);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        viewModel.getImportSucceeded().observe(getViewLifecycleOwner(), succeeded -> {
            if (Boolean.TRUE.equals(succeeded)) {
                NavHostFragment.findNavController(this).navigate(R.id.action_importPrompt_to_salary);
            }
        });

        viewModel.getImportError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.button_import_file).setOnClickListener(v ->
                importLauncher.launch(new String[]{"application/json"}));

        view.findViewById(R.id.button_skip_import).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_importPrompt_to_salary));
    }
}
