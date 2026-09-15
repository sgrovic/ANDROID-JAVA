package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;

import dagger.hilt.android.AndroidEntryPoint;

/** Screen 1: Welcome. "Get started" advances to the import-data prompt. */
@AndroidEntryPoint
public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        super(R.layout.fragment_welcome);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_get_started).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_welcome_to_importPrompt));

        view.findViewById(R.id.button_what_is_cutoff).setOnClickListener(v ->
                new com.app.cutoff.ui.common.CutoffSheetBuilder(requireContext())
                        .setTitle("How cutoffs work")
                        .setMessage(R.string.onboarding_cutoff_explanation)
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show());
    }
}
