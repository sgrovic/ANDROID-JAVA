package com.app.cutoff.ui.bills.adapter;

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
import com.app.cutoff.utils.BankGradientUtils;

/**
 * Screen 9: Fixed bills management. Shows every FixedBillEntity template
 * (persisted, type=FIXED, isTemplate=true) with edit/delete actions.
 * Distinct from ui.cutoff.adapter.FixedBillAdapter, which shows read-only
 * per-cutoff snapshots instead of editable templates.
 */
public class BillsAdapter extends ListAdapter<BillEntity, BillsAdapter.ViewHolder> {

    public interface OnTemplateActionListener {
        void onEdit(BillEntity template);

        void onDelete(BillEntity template);
    }

    private final OnTemplateActionListener listener;

    public BillsAdapter(OnTemplateActionListener listener) {
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
                            && oldItem.getAmount() == newItem.getAmount()
                            && oldItem.isActive() == newItem.isActive()
                            && java.util.Objects.equals(oldItem.getBank(), newItem.getBank());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fixed_bill_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillEntity template = getItem(position);
        holder.name.setText(template.getName());
        holder.amount.setText(CurrencyUtils.format(template.getAmount()));
        holder.bank.setText(template.getBank());
        BankGradientUtils.apply(holder.cardBackground, holder.name, holder.amount, holder.bank, holder.menuButton, template.getBank());
        holder.menuButton.setOnClickListener(v -> showMenu(v, template));
    }

    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;

    private void showMenu(View anchor, BillEntity template) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add(0, MENU_EDIT, 0, R.string.action_edit);
        popup.getMenu().add(0, MENU_DELETE, 1, R.string.action_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_EDIT) {
                listener.onEdit(template);
            } else if (item.getItemId() == MENU_DELETE) {
                listener.onDelete(template);
            }
            return true;
        });
        popup.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView amount;
        final TextView bank;
        final View menuButton;
        final View cardBackground;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_bill_name);
            amount = itemView.findViewById(R.id.text_bill_amount);
            bank = itemView.findViewById(R.id.text_bill_bank);
            menuButton = itemView.findViewById(R.id.button_bill_menu);
            cardBackground = itemView.findViewById(R.id.bill_card_background);
        }
    }
}
