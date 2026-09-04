package com.app.cutoff.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.data.database.pojo.CutoffSummary;
import com.app.cutoff.utils.CurrencyUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * "Past cutoffs" list on Home (screen 6). Each row shows the period label,
 * salary, and remaining amount — tapping opens Cutoff Detail (read-only
 * for past periods). Backed by CutoffSummary (salary + expense/incentive
 * totals already joined in by the DAO query) rather than the bare
 * CutoffEntity, so "remaining" doesn't need a per-row query of its own.
 */
public class HistoryAdapter extends ListAdapter<CutoffSummary, HistoryAdapter.ViewHolder> {

    public interface OnCutoffClickListener {
        void onCutoffClick(CutoffSummary cutoff);
    }

    private final OnCutoffClickListener listener;

    public HistoryAdapter(OnCutoffClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CutoffSummary> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CutoffSummary>() {
                @Override
                public boolean areItemsTheSame(@NonNull CutoffSummary oldItem, @NonNull CutoffSummary newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CutoffSummary oldItem, @NonNull CutoffSummary newItem) {
                    return oldItem.getSalarySnapshot() == newItem.getSalarySnapshot()
                            && oldItem.getPeriodEndEpochDay() == newItem.getPeriodEndEpochDay()
                            && oldItem.getTotalExpenses() == newItem.getTotalExpenses()
                            && oldItem.getTotalIncentives() == newItem.getTotalIncentives();
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_cutoff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CutoffSummary cutoff = getItem(position);
        LocalDate periodEnd = LocalDate.ofEpochDay(cutoff.getPeriodEndEpochDay());

        holder.periodLabel.setText(periodEnd.format(DateTimeFormatter.ofPattern("MMMM d")));
        holder.salaryLabel.setText("Salary " + CurrencyUtils.format(cutoff.getSalarySnapshot()));
        holder.remainingLabel.setText(CurrencyUtils.format(cutoff.getRemaining()) + " left");
        holder.itemView.setOnClickListener(v -> listener.onCutoffClick(cutoff));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView periodLabel;
        final TextView salaryLabel;
        final TextView remainingLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            periodLabel = itemView.findViewById(R.id.text_period_label);
            salaryLabel = itemView.findViewById(R.id.text_salary_label);
            remainingLabel = itemView.findViewById(R.id.text_remaining_label);
        }
    }
}
