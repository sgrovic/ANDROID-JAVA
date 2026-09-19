package com.app.cutoff.ui.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.BudgetProjectEntity;
import com.app.cutoff.data.database.entity.PlannedItemEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.ui.common.CutoffSheetBuilder;
import com.app.cutoff.utils.CurrencyUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProjectDetailFragment extends Fragment {
    public static final String ARG_PROJECT_ID = "arg_project_id";

    private ProjectDetailViewModel viewModel;
    private long projectId;
    private BudgetProjectEntity project;
    private SalaryEntity salary;
    private List<BillEntity> bills = Collections.emptyList();
    private List<PlannedItemEntity> plans = Collections.emptyList();

    private TextView projectTitle;
    private TextView firstSalary, secondSalary, firstFixed, secondFixed, firstPlan, secondPlan;
    private TextView firstLeft, secondLeft, firstCommitted, secondCommitted, planCount, planEmpty;
    private ProgressBar firstProgress, secondProgress;
    private LinearLayout planContainer;

    public ProjectDetailFragment() { super(R.layout.fragment_project); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        projectId = requireArguments().getLong(ARG_PROJECT_ID);
        viewModel = new ViewModelProvider(this).get(ProjectDetailViewModel.class);
        bindViews(view);
        view.findViewById(R.id.project_back).setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.project_more).setOnClickListener(this::showProjectMenu);
        view.findViewById(R.id.button_add_planned_item)
                .setOnClickListener(ignored -> showContributionDialog(null));
        view.findViewById(R.id.button_optimize_contributions)
                .setOnClickListener(ignored -> showOptimizationPreview());

        viewModel.project(projectId).observe(getViewLifecycleOwner(), value -> {
            project = value;
            if (value != null) render();
        });
        viewModel.salary().observe(getViewLifecycleOwner(), value -> { salary = value; render(); });
        viewModel.fixed().observe(getViewLifecycleOwner(), value -> {
            bills = value == null ? Collections.emptyList() : value; render();
        });
        viewModel.plans(projectId).observe(getViewLifecycleOwner(), value -> {
            plans = value == null ? Collections.emptyList() : value; render();
        });
    }

    private void bindViews(View view) {
        projectTitle = view.findViewById(R.id.project_title);
        firstSalary = view.findViewById(R.id.project_first_salary);
        secondSalary = view.findViewById(R.id.project_second_salary);
        firstFixed = view.findViewById(R.id.project_first_fixed);
        secondFixed = view.findViewById(R.id.project_second_fixed);
        firstPlan = view.findViewById(R.id.project_first_plan);
        secondPlan = view.findViewById(R.id.project_second_plan);
        firstLeft = view.findViewById(R.id.project_first_left);
        secondLeft = view.findViewById(R.id.project_second_left);
        firstCommitted = view.findViewById(R.id.project_first_committed);
        secondCommitted = view.findViewById(R.id.project_second_committed);
        firstProgress = view.findViewById(R.id.project_first_progress);
        secondProgress = view.findViewById(R.id.project_second_progress);
        planCount = view.findViewById(R.id.project_plan_count);
        planEmpty = view.findViewById(R.id.project_plan_empty);
        planContainer = view.findViewById(R.id.project_plan_container);
    }

    private void render() {
        if (project == null || projectTitle == null) return;
        projectTitle.setText(project.getName());
        double salaryFirst = salary == null ? 0 : salary.getFirstToFifteenth();
        double salarySecond = salary == null ? 0 : salary.getSixteenthToEnd();
        double fixedFirst = 0, fixedSecond = 0, plannedFirst = 0, plannedSecond = 0;
        for (BillEntity bill : bills) {
            if (!bill.isActive()) continue;
            if (!BillEntity.RECURRENCE_SECOND_CUTOFF.equals(bill.getRecurrenceSchedule())) {
                fixedFirst += bill.getAmount();
            }
            if (!BillEntity.RECURRENCE_FIRST_CUTOFF.equals(bill.getRecurrenceSchedule())) {
                fixedSecond += bill.getAmount();
            }
        }
        for (PlannedItemEntity plan : plans) {
            if (plan.isApplyFirstCutoff() && plan.isApplySecondCutoff()) {
                double firstShare = plan.getTotalAmount()
                        * normalizedFirstPercent(plan) / 100.0;
                plannedFirst += firstShare;
                plannedSecond += plan.getTotalAmount() - firstShare;
            } else if (plan.isApplyFirstCutoff()) {
                plannedFirst += plan.getTotalAmount();
            } else if (plan.isApplySecondCutoff()) {
                plannedSecond += plan.getTotalAmount();
            }
        }
        setIncome(firstSalary, salaryFirst);
        setIncome(secondSalary, salarySecond);
        setOutflow(firstFixed, fixedFirst);
        setOutflow(secondFixed, fixedSecond);
        setOutflow(firstPlan, plannedFirst);
        setOutflow(secondPlan, plannedSecond);
        setBalance(firstLeft, salaryFirst - fixedFirst - plannedFirst);
        setBalance(secondLeft, salarySecond - fixedSecond - plannedSecond);
        setCommitment(firstProgress, firstCommitted, salaryFirst, fixedFirst + plannedFirst);
        setCommitment(secondProgress, secondCommitted, salarySecond, fixedSecond + plannedSecond);
        renderPlans();
    }

    private void setIncome(TextView view, double amount) {
        view.setText(CurrencyUtils.format(amount)); view.setTextColor(primaryColor());
    }
    private void setOutflow(TextView view, double amount) {
        view.setText("\u2212" + CurrencyUtils.format(Math.abs(amount)));
        view.setTextColor(requireContext().getColor(R.color.project_negative));
    }
    private void setBalance(TextView view, double amount) {
        view.setText(amount < 0 ? "\u2212" + CurrencyUtils.format(Math.abs(amount))
                : CurrencyUtils.format(amount));
        view.setTextColor(amount < 0 ? requireContext().getColor(R.color.project_negative)
                : primaryColor());
    }
    private int primaryColor() {
        return MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorPrimary, 0);
    }
    private void setCommitment(ProgressBar bar, TextView label, double income, double committed) {
        int percent = income <= 0 ? 0 : (int) Math.round(committed / income * 100);
        bar.setProgress(Math.max(0, Math.min(100, percent)));
        label.setText(String.format(Locale.getDefault(), "%d%% committed", percent));
    }

    private void renderPlans() {
        planContainer.removeAllViews();
        planCount.setText(String.format(Locale.getDefault(), "%d active", plans.size()));
        planEmpty.setVisibility(plans.isEmpty() ? View.VISIBLE : View.GONE);
        planContainer.setVisibility(plans.isEmpty() ? View.GONE : View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (PlannedItemEntity plan : plans) {
            View row = inflater.inflate(R.layout.item_project_plan, planContainer, false);
            ((TextView) row.findViewById(R.id.project_plan_name)).setText(plan.getName());
            ((TextView) row.findViewById(R.id.project_plan_subtitle)).setText(planSubtitle(plan));
            ((ImageButton) row.findViewById(R.id.project_plan_more))
                    .setOnClickListener(anchor -> showPlanMenu(anchor, plan));
            planContainer.addView(row);
        }
    }

    private String planSubtitle(PlannedItemEntity plan) {
        String cutoff;
        if (plan.isApplyFirstCutoff() && plan.isApplySecondCutoff()) {
            int firstPercent = normalizedFirstPercent(plan);
            cutoff = "Both cutoffs \u2022 " + firstPercent + "/" + (100 - firstPercent);
        } else {
            cutoff = plan.isApplyFirstCutoff() ? "15th cutoff" : "30th cutoff";
        }
        return cutoff + " \u2022 " + CurrencyUtils.format(plan.getTotalAmount());
    }

    private int normalizedFirstPercent(PlannedItemEntity plan) {
        int value = plan.getFirstCutoffPercent();
        return value < 0 || value > 100 ? 50 : value;
    }

    private void showOptimizationPreview() {
        if (plans.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Nothing to optimize")
                    .setMessage("Add at least one contribution first.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        double salaryFirst = salary == null ? 0 : salary.getFirstToFifteenth();
        double salarySecond = salary == null ? 0 : salary.getSixteenthToEnd();
        double fixedFirst = 0;
        double fixedSecond = 0;
        for (BillEntity bill : bills) {
            if (!bill.isActive()) continue;
            if (!BillEntity.RECURRENCE_SECOND_CUTOFF.equals(bill.getRecurrenceSchedule())) {
                fixedFirst += bill.getAmount();
            }
            if (!BillEntity.RECURRENCE_FIRST_CUTOFF.equals(bill.getRecurrenceSchedule())) {
                fixedSecond += bill.getAmount();
            }
        }

        double currentFirst = 0;
        double currentSecond = 0;
        double contributionTotal = 0;
        for (PlannedItemEntity plan : plans) {
            contributionTotal += plan.getTotalAmount();
            if (plan.isApplyFirstCutoff() && plan.isApplySecondCutoff()) {
                double firstShare = plan.getTotalAmount()
                        * normalizedFirstPercent(plan) / 100.0;
                currentFirst += firstShare;
                currentSecond += plan.getTotalAmount() - firstShare;
            } else if (plan.isApplyFirstCutoff()) {
                currentFirst += plan.getTotalAmount();
            } else if (plan.isApplySecondCutoff()) {
                currentSecond += plan.getTotalAmount();
            }
        }

        double availableFirst = salaryFirst - fixedFirst;
        double availableSecond = salarySecond - fixedSecond;
        int firstPercent = ContributionOptimizer.recommendedFirstPercent(
                availableFirst, availableSecond, contributionTotal);

        double optimizedFirstContribution = contributionTotal * firstPercent / 100.0;
        double currentFirstBalance = availableFirst - currentFirst;
        double currentSecondBalance = availableSecond - currentSecond;
        double optimizedFirstBalance = availableFirst - optimizedFirstContribution;
        double optimizedSecondBalance = availableSecond
                - (contributionTotal - optimizedFirstContribution);

        View content = getLayoutInflater().inflate(
                R.layout.dialog_optimize_contributions, null);
        ((TextView) content.findViewById(R.id.optimize_current_first))
                .setText("15th  " + formatSignedBalance(currentFirstBalance));
        ((TextView) content.findViewById(R.id.optimize_current_second))
                .setText("30th  " + formatSignedBalance(currentSecondBalance));
        ((TextView) content.findViewById(R.id.optimize_new_first))
                .setText("15th  " + formatSignedBalance(optimizedFirstBalance));
        ((TextView) content.findViewById(R.id.optimize_new_second))
                .setText("30th  " + formatSignedBalance(optimizedSecondBalance));

        LinearLayout changes = content.findViewById(R.id.optimize_changes_container);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (PlannedItemEntity plan : plans) {
            View row = inflater.inflate(R.layout.item_optimization_change, changes, false);
            ((TextView) row.findViewById(R.id.optimize_change_name)).setText(plan.getName());
            ((TextView) row.findViewById(R.id.optimize_change_subtitle))
                    .setText(optimizationSubtitle(firstPercent));
            changes.addView(row);
        }

        final int appliedPercent = firstPercent;
        new CutoffSheetBuilder(requireContext())
                .setTitle("Optimize contributions")
                .setView(content)
                .setPositiveButton(R.string.action_apply_optimization,
                        (dialog, which) -> viewModel.applyOptimization(plans, appliedPercent))
                .setNegativeButton(R.string.action_cancel, null)
                .create().show();
    }

    private String optimizationSubtitle(int firstPercent) {
        if (firstPercent <= 0) return "30th cutoff";
        if (firstPercent >= 100) return "15th cutoff";
        return String.format(Locale.getDefault(), "Both cutoffs \u2022 %d%% / %d%%",
                firstPercent, 100 - firstPercent);
    }

    private String formatSignedBalance(double amount) {
        return amount < 0 ? "\u2212" + CurrencyUtils.format(Math.abs(amount))
                : CurrencyUtils.format(amount);
    }

    private void showPlanMenu(View anchor, PlannedItemEntity plan) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(R.string.action_edit);
        menu.getMenu().add(R.string.action_delete);
        menu.setOnMenuItemClickListener(item -> {
            if (getString(R.string.action_edit).contentEquals(item.getTitle())) {
                showContributionDialog(plan);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete contribution?")
                        .setMessage("Remove " + plan.getName() + " from this project?")
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_delete,
                                (dialog, which) -> viewModel.delete(plan)).show();
            }
            return true;
        });
        menu.show();
    }

    private void showContributionDialog(@Nullable PlannedItemEntity contribution) {
        View content = getLayoutInflater().inflate(R.layout.dialog_add_planned_item, null);
        EditText name = content.findViewById(R.id.input_plan_name);
        EditText amount = content.findViewById(R.id.input_plan_amount);
        RadioGroup cutoff = content.findViewById(R.id.input_plan_cutoff);
        View splitSection = content.findViewById(R.id.plan_split_section);
        Slider ratioSlider = content.findViewById(R.id.input_plan_ratio_slider);
        TextView firstPercentLabel = content.findViewById(R.id.plan_first_percent);
        TextView secondPercentLabel = content.findViewById(R.id.plan_second_percent);
        TextView firstPreview = content.findViewById(R.id.plan_first_preview);
        TextView secondPreview = content.findViewById(R.id.plan_second_preview);
        if (contribution != null) {
            name.setText(contribution.getName());
            amount.setText(String.valueOf(contribution.getTotalAmount()));
            if (contribution.isApplyFirstCutoff() && contribution.isApplySecondCutoff()) {
                cutoff.check(R.id.radio_plan_both);
                ratioSlider.setValue(normalizedFirstPercent(contribution));
            } else if (contribution.isApplyFirstCutoff()) {
                cutoff.check(R.id.radio_plan_first);
            } else {
                cutoff.check(R.id.radio_plan_second);
            }
        }
        Runnable refreshSplit = () -> updateSplitControls(cutoff, splitSection, ratioSlider,
                firstPercentLabel, secondPercentLabel, amount, firstPreview, secondPreview);
        cutoff.setOnCheckedChangeListener((group, checkedId) -> refreshSplit.run());
        ratioSlider.addOnChangeListener((slider, value, fromUser) -> refreshSplit.run());
        amount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { refreshSplit.run(); }
        });
        refreshSplit.run();
        new CutoffSheetBuilder(requireContext())
                .setTitle(contribution == null ? "Add contribution" : "Edit contribution")
                .setView(content)
                .setPositiveButton(contribution == null ? R.string.action_add : R.string.action_save,
                        (dialog, which) -> {
                    try {
                        String itemName = name.getText().toString().trim();
                        double total = Double.parseDouble(amount.getText().toString());
                        int selected = cutoff.getCheckedRadioButtonId();
                        boolean first = selected == R.id.radio_plan_first
                                || selected == R.id.radio_plan_both;
                        boolean second = selected == R.id.radio_plan_second
                                || selected == R.id.radio_plan_both;
                        int firstCutoffPercent = first && second
                                ? Math.round(ratioSlider.getValue()) : first ? 100 : 0;
                        if (!itemName.isEmpty() && total > 0 && (first || second)) {
                            if (contribution == null) {
                                viewModel.add(projectId, itemName, total, first, second,
                                        firstCutoffPercent);
                            } else {
                                contribution.setName(itemName);
                                contribution.setTotalAmount(total);
                                contribution.setApplyFirstCutoff(first);
                                contribution.setApplySecondCutoff(second);
                                contribution.setFirstCutoffPercent(firstCutoffPercent);
                                viewModel.update(contribution);
                            }
                        }
                    } catch (NumberFormatException ignored) { }
                })
                .setNegativeButton(R.string.action_cancel, null).create().show();
    }

    private void updateSplitControls(RadioGroup cutoff, View splitSection, Slider ratioSlider,
                                     TextView firstPercent, TextView secondPercent, EditText amount,
                                     TextView firstPreview, TextView secondPreview) {
        boolean both = cutoff.getCheckedRadioButtonId() == R.id.radio_plan_both;
        splitSection.setVisibility(both ? View.VISIBLE : View.GONE);
        if (!both) return;
        double total = 0;
        try { total = Double.parseDouble(amount.getText().toString()); }
        catch (NumberFormatException ignored) { }
        int firstShare = Math.round(ratioSlider.getValue());
        firstPercent.setText(String.format(Locale.getDefault(), "15th \u2022 %d%%", firstShare));
        secondPercent.setText(String.format(Locale.getDefault(), "30th \u2022 %d%%",
                100 - firstShare));
        double firstAmount = total * firstShare / 100.0;
        firstPreview.setText(CurrencyUtils.format(firstAmount));
        secondPreview.setText(CurrencyUtils.format(total - firstAmount));
    }

    private void showProjectMenu(View anchor) {
        if (project == null) return;
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(R.string.action_edit);
        menu.getMenu().add(R.string.action_delete);
        menu.setOnMenuItemClickListener(item -> {
            if (getString(R.string.action_edit).contentEquals(item.getTitle())) showEditProject();
            else confirmDeleteProject();
            return true;
        });
        menu.show();
    }

    private void showEditProject() {
        View content = getLayoutInflater().inflate(R.layout.dialog_budget_project, null);
        EditText name = content.findViewById(R.id.input_project_name);
        name.setText(project.getName());
        new CutoffSheetBuilder(requireContext()).setTitle("Edit budget project")
                .setView(content).setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String value = name.getText().toString().trim();
                    if (!value.isEmpty()) {
                        project.setName(value);
                        viewModel.updateProject(project);
                    }
                }).setNegativeButton(R.string.action_cancel, null).create().show();
    }

    private void confirmDeleteProject() {
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Delete project?")
                .setMessage("Delete " + project.getName() + " and all of its contributions?")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteProject(project);
                    NavHostFragment.findNavController(this).navigateUp();
                }).show();
    }
}
