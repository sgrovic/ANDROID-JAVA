package com.app.cutoff.ui.home.fragment;

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
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.navigation.NavigationManager;
import com.app.cutoff.ui.home.adapter.HistoryAdapter;
import com.app.cutoff.ui.home.adapter.UpcomingCutoffAdapter;
import com.app.cutoff.ui.home.viewmodel.HomeViewModel;
import com.app.cutoff.utils.CurrencyUtils;
import com.app.cutoff.utils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 6: Home. Shows the upcoming (current) cutoff — salary, expenses,
 * remaining, spending progress — plus a "Past cutoffs" list. Tapping the
 * current cutoff card or a past-cutoff row opens Cutoff Detail.
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private NavigationManager navigationManager;
    private HistoryAdapter historyAdapter;
    private UpcomingCutoffAdapter upcomingCutoffAdapter;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        navigationManager = new NavigationManager(NavHostFragment.findNavController(this));

        TextView cutoffLabel = view.findViewById(R.id.text_upcoming_cutoff_label);
        TextView daysLeft = view.findViewById(R.id.text_days_left);
        TextView salaryValue = view.findViewById(R.id.text_salary_value);
        TextView totalBillValue = view.findViewById(R.id.text_total_bill_value);
        TextView remainingValue = view.findViewById(R.id.text_remaining_value);
        ProgressBar spendingProgress = view.findViewById(R.id.progress_spending);
        TextView spendingPercentLabel = view.findViewById(R.id.text_spending_percent);
        View upcomingCutoffCard = view.findViewById(R.id.card_upcoming_cutoff);

        RecyclerView pastCutoffsList = view.findViewById(R.id.recycler_past_cutoffs);
        pastCutoffsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new HistoryAdapter(cutoff ->
                navigationManager.toCutoffDetail(cutoff.getId()));
        pastCutoffsList.setAdapter(historyAdapter);

        RecyclerView upcomingCutoffsList = view.findViewById(R.id.recycler_upcoming_cutoffs);
        upcomingCutoffsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingCutoffAdapter = new UpcomingCutoffAdapter(periodEnd ->
                viewModel.ensureCutoffForDate(periodEnd, cutoffId ->
                        requireActivity().runOnUiThread(() ->
                                navigationManager.toCutoffDetail(cutoffId))));
        upcomingCutoffsList.setAdapter(upcomingCutoffAdapter);

        TextView seeMoreUpcoming = view.findViewById(R.id.text_see_more_upcoming);
        seeMoreUpcoming.setOnClickListener(v -> {
            boolean expanded = !upcomingCutoffAdapter.isExpanded();
            upcomingCutoffAdapter.setExpanded(expanded);
            seeMoreUpcoming.setText(expanded ? "See less" : "See more");
        });

        // Track the latest known values from each source and recompute on every change.
        // Reading back through viewModel.getXxx().getValue() here would create/read a
        // fresh, never-activated LiveData and always see null — instead we keep the
        // values passed into each observer callback and combine them directly.
        final double[] latestSalary = {0};
        final double[] latestExpenses = {0};
        final double[] latestIncentives = {0};

        Runnable updateTotals = () -> {
            totalBillValue.setText(CurrencyUtils.format(latestExpenses[0]));

            double remaining = latestSalary[0] + latestIncentives[0] - latestExpenses[0];
            remainingValue.setText(CurrencyUtils.format(remaining));

            double totalAvailable = latestSalary[0] + latestIncentives[0];
            salaryValue.setText(CurrencyUtils.format(totalAvailable));

            int percent = totalAvailable > 0
                    ? (int) Math.min(100, Math.max(0, Math.round((latestExpenses[0] / totalAvailable) * 100)))
                    : 0;
            spendingProgress.setProgress(percent);
            spendingPercentLabel.setText(percent + "%");
        };

        viewModel.getCurrentCutoff().observe(getViewLifecycleOwner(), cutoff -> {
            if (cutoff == null) return;
            bindCurrentCutoffHeader(cutoff, cutoffLabel, daysLeft, salaryValue);
            upcomingCutoffCard.setOnClickListener(v -> navigationManager.toCutoffDetail(cutoff.getId()));
            latestSalary[0] = cutoff.getSalarySnapshot();
            updateTotals.run();

            LocalDate currentPeriodEnd = LocalDate.ofEpochDay(cutoff.getPeriodEndEpochDay());
            upcomingCutoffAdapter.submitList(
                    DateUtils.upcomingPeriodEndsThroughYearEnd(currentPeriodEnd));
            seeMoreUpcoming.setVisibility(
                    upcomingCutoffAdapter.hasMore() ? View.VISIBLE : View.GONE);
            seeMoreUpcoming.setText("See more");
        });

        viewModel.getCurrentCutoffExpenses().observe(getViewLifecycleOwner(), expenses -> {
            latestExpenses[0] = expenses != null ? expenses : 0;
            updateTotals.run();
        });

        viewModel.getCurrentCutoffIncentives().observe(getViewLifecycleOwner(), incentives -> {
            latestIncentives[0] = incentives != null ? incentives : 0;
            updateTotals.run();
        });

        viewModel.getPastCutoffs().observe(getViewLifecycleOwner(), historyAdapter::submitList);
    }

    private void bindCurrentCutoffHeader(CutoffEntity cutoff, TextView cutoffLabel, TextView daysLeft,
                                          TextView salaryValue) {
        LocalDate periodEnd = LocalDate.ofEpochDay(cutoff.getPeriodEndEpochDay());
        LocalDate today = LocalDate.now();

        cutoffLabel.setText(periodEnd.format(DateTimeFormatter.ofPattern("MMMM d")));
        long remainingDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(today, periodEnd));
        daysLeft.setText(getResources().getQuantityString(
                R.plurals.days_left, (int) remainingDays, remainingDays));

        salaryValue.setText(CurrencyUtils.format(cutoff.getSalarySnapshot()));
    }
}
