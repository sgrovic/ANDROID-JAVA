package com.app.cutoff.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Currently unused: HomeFragment binds its single "upcoming cutoff" card
 * directly rather than through a RecyclerView, since there's only one.
 * Kept as a placeholder for if Home grows into multiple dashboard cards
 * (e.g. a savings-goal card, a spending-by-category card) that would
 * benefit from a shared card-list adapter instead of one-off view lookups.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.CardViewHolder> {

    public static class DashboardCard {
        public final int layoutRes;

        public DashboardCard(int layoutRes) {
            this.layoutRes = layoutRes;
        }
    }

    private final List<DashboardCard> cards = new ArrayList<>();

    public void submitCards(List<DashboardCard> newCards) {
        cards.clear();
        cards.addAll(newCards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return cards.get(position).layoutRes;
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // Per-card binding would go here once multiple card types exist.
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        CardViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
