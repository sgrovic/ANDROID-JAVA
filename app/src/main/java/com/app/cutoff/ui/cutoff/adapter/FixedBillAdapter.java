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
import com.app.cutoff.utils.BankGradientUtils;

/**
 * Renders the "Fixed bills" section rows within Cutoff Detail — these are
 * BillEntity snapshots (type=FIXED, isTemplate=false) copied in when the
 * cutoff was generated, NOT the templates themselves. Editing a row here
 * only affects this cutoff.
 */
public class FixedBillAdapter extends ListAdapter<BillEntity, FixedBillAdapter.ViewHolder> {

    public interface OnBillActionListener {
        void onEdit(BillEntity bill);

        void onDelete(BillEntity bill);
    }

    private final OnBillActionListener listener;

    public FixedBillAdapter(OnBillActionListener listener) {
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
                            && oldItem.getBank().equals(newItem.getBank())
                            && oldItem.getPaymentStatus().equals(newItem.getPaymentStatus());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fixed_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillEntity bill = getItem(position);
        holder.name.setText(bill.getName());
        holder.amount.setText(CurrencyUtils.format(bill.getAmount()));
        TextView status = holder.itemView.findViewById(R.id.text_bill_status);
        status.setText(bill.getPaymentStatus());
        status.setTextColor(android.graphics.Color.parseColor(
                "PAID".equals(bill.getPaymentStatus()) ? "#9AD9A8" : "#E7D58A"));
        holder.bank.setText(bill.getBank());
        BankGradientUtils.apply(holder.itemView, holder.name, holder.amount, holder.bank, holder.menuButton, bill.getBank());
        holder.menuButton.setOnClickListener(v -> showBillMenu(v, bill));
        holder.itemView.setOnClickListener(v -> listener.onEdit(bill));
    }

    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;

    private void showBillMenu(View anchor, BillEntity bill) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(anchor.getContext(), anchor);
        popup.getMenu().add(0, MENU_EDIT, 0, R.string.action_edit);
        popup.getMenu().add(0, MENU_DELETE, 1, R.string.action_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_EDIT) {
                listener.onEdit(bill);
            } else if (item.getItemId() == MENU_DELETE) {
                listener.onDelete(bill);
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_bill_name);
            amount = itemView.findViewById(R.id.text_bill_amount);
            bank = itemView.findViewById(R.id.text_bill_bank);
            menuButton = itemView.findViewById(R.id.button_bill_menu);
        }
    }
}
