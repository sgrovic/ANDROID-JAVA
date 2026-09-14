package com.app.cutoff.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * "Upcoming cutoffs" list on Home. Unlike HistoryAdapter (backed by real
 * CutoffEntity rows), this shows period-end dates computed on the fly via
 * DateUtils.upcomingPeriodEndsThroughYearEnd() — those periods don't have
 * database rows yet since a CutoffEntity is only created once its period
 * actually starts. Rows are clickable so Home can materialize the selected
 * future cutoff and open its detail screen for planning.
 */
public class UpcomingCutoffAdapter extends RecyclerView.Adapter<UpcomingCutoffAdapter.ViewHolder> {

    public interface OnCutoffClickListener {
        void onCutoffClick(LocalDate periodEnd);
    }

    private final List<LocalDate> allPeriodEnds = new ArrayList<>();
    private final OnCutoffClickListener clickListener;

    public UpcomingCutoffAdapter(OnCutoffClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<LocalDate> newPeriodEnds) {
        allPeriodEnds.clear();
        if (newPeriodEnds != null) {
            allPeriodEnds.addAll(newPeriodEnds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_cutoff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate periodEnd = allPeriodEnds.get(position);
        holder.periodLabel.setText(com.app.cutoff.utils.DateUtils.periodStart(periodEnd)
                .format(DateTimeFormatter.ofPattern("MMM d")) + "–"
                + periodEnd.format(DateTimeFormatter.ofPattern("d, yyyy")));
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCutoffClick(periodEnd);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allPeriodEnds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView periodLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            periodLabel = itemView.findViewById(R.id.text_upcoming_period_label);
        }
    }
}
