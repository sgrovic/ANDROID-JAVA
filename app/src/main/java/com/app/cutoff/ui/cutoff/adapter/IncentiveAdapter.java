package com.app.cutoff.ui.cutoff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.utils.CurrencyUtils;

/**
 * Renders the "Incentives" section rows in Cutoff Detail (Retro, Bonus, etc.).
 * Uses the same single dropdown (edit/delete) menu pattern as FixedBillAdapter
 * and VariableBillAdapter, instead of separate edit/delete buttons.
 */
public class IncentiveAdapter extends ListAdapter<BillEntity, IncentiveAdapter.ViewHolder> {

    public interface OnIncentiveActionListener {
        void onEdit(BillEntity incentive);

        void onDelete(BillEntity incentive);
    }

    private final OnIncentiveActionListener listener;

    public IncentiveAdapter(OnIncentiveActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<BillEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BillEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull BillEntity oldItem, @NonNull BillEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull BillEntity oldItem, @NonNull BillEntity newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getAmount() == newItem.getAmount();
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incentive, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillEntity incentive = getItem(position);
        holder.name.setText(incentive.getName());
        holder.amount.setText(CurrencyUtils.format(incentive.getAmount()));
        holder.menuButton.setOnClickListener(v -> showIncentiveMenu(v, incentive));
    }

    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;

    private void showIncentiveMenu(View anchor, BillEntity incentive) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add(0, MENU_EDIT, 0, R.string.action_edit);
        popup.getMenu().add(0, MENU_DELETE, 1, R.string.action_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_EDIT) {
                listener.onEdit(incentive);
            } else if (item.getItemId() == MENU_DELETE) {
                listener.onDelete(incentive);
            }
            return true;
        });
        popup.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView amount;
        final View menuButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_incentive_name);
            amount = itemView.findViewById(R.id.text_incentive_amount);
            menuButton = itemView.findViewById(R.id.button_incentive_menu);
        }
    }
}
