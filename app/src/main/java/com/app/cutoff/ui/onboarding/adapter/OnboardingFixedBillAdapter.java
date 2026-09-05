package com.app.cutoff.ui.onboarding.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.ui.onboarding.model.OnboardingState;
import com.app.cutoff.utils.CurrencyUtils;
import com.app.cutoff.utils.BankGradientUtils;

import java.util.List;

/**
 * Shows the list of draft fixed bills being assembled during onboarding
 * (screen 4) — plain OnboardingState.DraftFixedBill objects, not yet
 * persisted. For the post-onboarding "manage fixed bills" list (screen 9,
 * backed by real BillEntity rows), see ui.bills.adapter.BillsAdapter.
 */
public class OnboardingFixedBillAdapter extends RecyclerView.Adapter<OnboardingFixedBillAdapter.ViewHolder> {

    public interface OnRemoveListener {
        void onRemove(OnboardingState.DraftFixedBill draft);
    }

    private final List<OnboardingState.DraftFixedBill> drafts;
    private final OnRemoveListener onRemoveListener;

    public OnboardingFixedBillAdapter(List<OnboardingState.DraftFixedBill> drafts, OnRemoveListener onRemoveListener) {
        this.drafts = drafts;
        this.onRemoveListener = onRemoveListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fixed_bill_draft, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnboardingState.DraftFixedBill draft = drafts.get(position);
        holder.name.setText(draft.name);
        holder.amount.setText(CurrencyUtils.format(draft.amount));
        holder.bank.setText(draft.bank);
        BankGradientUtils.apply(holder.itemView, holder.name, holder.amount, holder.bank, holder.removeButton, draft.bank);
        holder.removeButton.setOnClickListener(v -> onRemoveListener.onRemove(draft));
    }

    @Override
    public int getItemCount() {
        return drafts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView amount;
        final TextView bank;
        final View removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_bill_name);
            amount = itemView.findViewById(R.id.text_bill_amount);
            bank = itemView.findViewById(R.id.text_bill_bank);
            removeButton = itemView.findViewById(R.id.button_remove_bill);
        }
    }
}
