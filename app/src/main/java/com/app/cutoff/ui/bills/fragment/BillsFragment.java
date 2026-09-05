package com.app.cutoff.ui.bills.fragment;

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
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.ui.bills.adapter.BillsAdapter;
import com.app.cutoff.ui.bills.dialog.AddBillDialog;
import com.app.cutoff.ui.bills.dialog.EditBillDialog;
import com.app.cutoff.ui.bills.viewmodel.BillsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 9: Fixed bills management (reached from Settings > Fixed bills).
 * Add/edit/delete recurring templates. Changes here only affect cutoffs
 * generated after the change — never past ones.
 */
@AndroidEntryPoint
public class BillsFragment extends Fragment {

    private BillsViewModel viewModel;

    public BillsFragment() {
        super(R.layout.fragment_bills);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BillsViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_fixed_bill_templates);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        View emptyState = view.findViewById(R.id.layout_empty_state);

        BillsAdapter adapter = new BillsAdapter(new BillsAdapter.OnTemplateActionListener() {
            @Override
            public void onEdit(BillEntity template) {
                EditBillDialog dialog = EditBillDialog.newInstance(template.getId(), template.getName(), template.getAmount(), template.getBank());
                dialog.setOnBillEditedListener((name, amount, bank) -> {
                    template.setName(name);
                    template.setAmount(amount);
                    template.setBank(bank);
                    viewModel.updateFixedBillTemplate(template);
                });
                dialog.show(getChildFragmentManager(), "edit_fixed_bill");
            }

            @Override
            public void onDelete(BillEntity template) {
                viewModel.deleteFixedBillTemplate(template);
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel.getFixedBillTemplates().observe(getViewLifecycleOwner(), templates -> {
            adapter.submitList(templates);
            boolean isEmpty = templates == null || templates.isEmpty();
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        view.findViewById(R.id.button_add_fixed_bill).setOnClickListener(v -> {
            AddBillDialog dialog = AddBillDialog.newInstance(AddBillDialog.Mode.FIXED_BILL_TEMPLATE);
            dialog.setOnBillAddedListener((name, amount, iconKey, bank) ->
                    viewModel.addFixedBillTemplate(name, String.valueOf(amount), iconKey, 0, bank));
            dialog.show(getChildFragmentManager(), "add_fixed_bill");
        });

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
    }
}
