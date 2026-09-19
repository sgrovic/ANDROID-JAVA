package com.app.cutoff.ui.onboarding.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.ui.bills.dialog.AddBillDialog;
import com.app.cutoff.ui.onboarding.adapter.OnboardingFixedBillAdapter;
import com.app.cutoff.ui.onboarding.viewmodel.OnboardingViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 5: Set fixed bills. Lets the user seed their initial set of
 * recurring bills (Grocery, Parent, Parking, etc.) before finishing setup.
 * These become FixedBillEntity templates once onboarding completes.
 * Pre-populated from OnboardingState if the user imported a backup file
 * on the earlier import-prompt screen.
 */
@AndroidEntryPoint
public class FixedBillsFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private OnboardingFixedBillAdapter adapter;

    public FixedBillsFragment() {
        super(R.layout.fragment_fixed_bills);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        if (viewModel.getState().isImported()) {
            TextView badge = view.findViewById(R.id.text_import_badge);
            int count = viewModel.getState().getDraftFixedBills().size();
            badge.setText(count + " bill" + (count == 1 ? "" : "s") + " imported");
            badge.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_fixed_bills);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OnboardingFixedBillAdapter(
                viewModel.getState().getDraftFixedBills(),
                draft -> {
                    viewModel.removeFixedBill(draft);
                    adapter.notifyDataSetChanged();
                }
        );
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_add_fixed_bill).setOnClickListener(v -> {
            AddBillDialog dialog = AddBillDialog.newInstance(AddBillDialog.Mode.FIXED_BILL_TEMPLATE);
            dialog.setOnBillAddedListener((name, amount, iconKey, bank, recurrenceSchedule) -> {
                viewModel.addFixedBill(name, amount, iconKey, bank, recurrenceSchedule);
                adapter.notifyDataSetChanged();
            });
            dialog.show(getChildFragmentManager(), "add_fixed_bill");
        });

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        view.findViewById(R.id.button_next).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_fixedBills_to_allSet));
    }
}
