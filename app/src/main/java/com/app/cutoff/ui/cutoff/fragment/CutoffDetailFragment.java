package com.app.cutoff.ui.cutoff.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.ui.bills.dialog.AddBillDialog;
import com.app.cutoff.ui.bills.dialog.EditBillDialog;
import com.app.cutoff.ui.cutoff.adapter.FixedBillAdapter;
import com.app.cutoff.ui.cutoff.adapter.IncentiveAdapter;
import com.app.cutoff.ui.cutoff.adapter.VariableBillAdapter;
import com.app.cutoff.ui.cutoff.dialog.AddIncentiveDialog;
import com.app.cutoff.ui.cutoff.viewmodel.CutoffViewModel;
import com.app.cutoff.utils.CurrencyUtils;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 7: Cutoff Detail. Header (salary/remaining/progress), Incentives
 * section, Fixed Bills section (snapshot rows), Variable Bills section,
 * plus "Add variable bill" / "Add incentive" actions.
 */
@AndroidEntryPoint
public class CutoffDetailFragment extends Fragment {

    private CutoffViewModel viewModel;

    public CutoffDetailFragment() {
        super(R.layout.fragment_cutoff_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CutoffViewModel.class);

        TextView salaryValue = view.findViewById(R.id.text_salary_value);
        TextView totalBillValue = view.findViewById(R.id.text_total_bill_value);
        TextView remainingValue = view.findViewById(R.id.text_remaining_value);
        ProgressBar spendingProgress = view.findViewById(R.id.progress_spending);
        TextView spendingPercentLabel = view.findViewById(R.id.text_spending_percent);
        TextView summaryRemaining = view.findViewById(R.id.text_summary_remaining);

        RecyclerView incentiveList = view.findViewById(R.id.recycler_incentives);
        RecyclerView fixedBillList = view.findViewById(R.id.recycler_fixed_bills);
        RecyclerView variableBillList = view.findViewById(R.id.recycler_variable_bills);

        incentiveList.setLayoutManager(new LinearLayoutManager(requireContext()));
        fixedBillList.setLayoutManager(new LinearLayoutManager(requireContext()));
        variableBillList.setLayoutManager(new LinearLayoutManager(requireContext()));

        IncentiveAdapter incentiveAdapter = new IncentiveAdapter(new IncentiveAdapter.OnIncentiveActionListener() {
            @Override
            public void onEdit(BillEntity incentive) {
                openEditBillDialog(incentive);
            }

            @Override
            public void onDelete(BillEntity incentive) {
                viewModel.deleteBill(incentive);
            }
        });
        incentiveList.setAdapter(incentiveAdapter);

        FixedBillAdapter fixedBillAdapter = new FixedBillAdapter(new FixedBillAdapter.OnBillActionListener() {
            @Override
            public void onEdit(BillEntity bill) {
                openEditBillDialog(bill);
            }

            @Override
            public void onDelete(BillEntity bill) {
                viewModel.deleteBill(bill);
            }
        });
        fixedBillList.setAdapter(fixedBillAdapter);

        VariableBillAdapter variableBillAdapter = new VariableBillAdapter(new VariableBillAdapter.OnBillActionListener() {
            @Override
            public void onEdit(BillEntity bill) {
                openEditBillDialog(bill);
            }

            @Override
            public void onDelete(BillEntity bill) {
                viewModel.deleteBill(bill);
            }
        });
        variableBillList.setAdapter(variableBillAdapter);

        viewModel.getIncentives().observe(getViewLifecycleOwner(), incentiveAdapter::submitList);
        viewModel.getFixedBills().observe(getViewLifecycleOwner(), fixedBillAdapter::submitList);
        viewModel.getVariableBills().observe(getViewLifecycleOwner(), variableBillAdapter::submitList);

        // Track the latest known values from each source and recompute on every change,
        // rather than reading back through viewModel.getXxx().getValue() (which would
        // always see null on a LiveData that was never separately activated).
        final double[] latestSalary = {0};
        final double[] latestExpenses = {0};
        final double[] latestIncentives = {0};

        Runnable updateTotals = () -> {
            totalBillValue.setText(CurrencyUtils.format(latestExpenses[0]));

            double remaining = latestSalary[0] + latestIncentives[0] - latestExpenses[0];
            remainingValue.setText(CurrencyUtils.format(remaining));
            summaryRemaining.setText(CurrencyUtils.format(remaining));

            double totalAvailable = latestSalary[0] + latestIncentives[0];
            salaryValue.setText(CurrencyUtils.format(totalAvailable));

            int percent = totalAvailable > 0
                    ? (int) Math.min(100, Math.max(0, Math.round((latestExpenses[0] / totalAvailable) * 100)))
                    : 0;
            spendingProgress.setProgress(percent);
            spendingPercentLabel.setText(percent + "%");
        };

        viewModel.getCutoff().observe(getViewLifecycleOwner(), cutoff -> {
            if (cutoff == null) return;
            java.time.LocalDate start = java.time.LocalDate.ofEpochDay(cutoff.getPeriodStartEpochDay());
            java.time.LocalDate end = java.time.LocalDate.ofEpochDay(cutoff.getPeriodEndEpochDay());
            java.time.LocalDate today = java.time.LocalDate.now();
            String period = start.format(java.time.format.DateTimeFormatter.ofPattern("MMM d")) + "–" + end.format(java.time.format.DateTimeFormatter.ofPattern("d, yyyy"));
            ((TextView) view.findViewById(R.id.text_cutoff_period)).setText(period);
            ((TextView) view.findViewById(R.id.text_cutoff_status)).setText(end.isBefore(today) ? "Past cutoff" : start.isAfter(today) ? "Upcoming cutoff" : "Current cutoff");
            ((TextView) view.findViewById(R.id.text_cutoff_scope)).setText("Changes apply only to " + period + ".");
            salaryValue.setText(CurrencyUtils.format(cutoff.getSalarySnapshot()));
            latestSalary[0] = cutoff.getSalarySnapshot();
            updateTotals.run();
        });

        viewModel.getTotalExpenses().observe(getViewLifecycleOwner(), expenses -> {
            latestExpenses[0] = expenses != null ? expenses : 0;
            updateTotals.run();
        });

        viewModel.getTotalIncentives().observe(getViewLifecycleOwner(), incentives -> {
            latestIncentives[0] = incentives != null ? incentives : 0;
            updateTotals.run();
        });

        view.findViewById(R.id.button_add_fixed_bill).setOnClickListener(v -> {
            AddBillDialog dialog = AddBillDialog.newInstance(AddBillDialog.Mode.FIXED_BILL);
            dialog.setOnBillAddedListener((name, amount, iconKey, bank) ->
                    viewModel.addFixedBill(name, String.valueOf(amount), bank));
            dialog.show(getChildFragmentManager(), "add_fixed_bill");
        });

        view.findViewById(R.id.button_add_variable_bill).setOnClickListener(v -> {
            AddBillDialog dialog = AddBillDialog.newInstance(AddBillDialog.Mode.VARIABLE_BILL);
            dialog.setOnBillAddedListener((name, amount, iconKey, bank) ->
                    viewModel.addVariableBill(name, String.valueOf(amount), bank));
            dialog.show(getChildFragmentManager(), "add_variable_bill");
        });

        view.findViewById(R.id.button_add_incentive).setOnClickListener(v -> {
            AddIncentiveDialog dialog = AddIncentiveDialog.newInstance();
            dialog.setOnIncentiveAddedListener((name, amount) ->
                    viewModel.addIncentive(name, String.valueOf(amount)));
            dialog.show(getChildFragmentManager(), "add_incentive");
        });

        view.findViewById(R.id.button_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
    }

    /** Shared edit flow for fixed bills, variable bills, and incentives alike. */
    private void openEditBillDialog(BillEntity bill) {
        EditBillDialog dialog = EditBillDialog.newInstance(bill.getId(), bill.getName(), bill.getAmount(), bill.getBank());
        dialog.setCutoffOnly(true);
        dialog.setOnBillEditedListener((name, amount, bank) -> {
            // IMPORTANT: don't mutate `bill` in place — it's the exact instance the
            // adapter's ListAdapter is still holding in its current submitted list.
            // If we edit it directly, DiffUtil.areContentsTheSame() ends up comparing
            // that already-mutated "old" item against the freshly-queried "new" item
            // from Room, sees them as equal, and skips rebinding the row — so the
            // RecyclerView keeps showing the stale text until something else forces a
            // full refresh. Building a separate copy keeps the adapter's old-list
            // reference untouched so the real diff (old text vs. new text) is detected.
            BillEntity updated = new BillEntity(
                    bill.getType(), name, amount, bill.getIconKey(), bill.isTemplate(),
                    bill.getCutoffId(), bill.getSourceBillId(), bill.isActive(), bill.isPaid(),
                    bill.getSortOrder(), bank);
            updated.setId(bill.getId());
            viewModel.updateBill(updated);
        });
        dialog.show(getChildFragmentManager(), "edit_bill");
    }
}
