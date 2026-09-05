package com.app.cutoff.ui.bills.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.app.cutoff.utils.Constants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.app.cutoff.R;
import com.app.cutoff.utils.ValidationUtils;

/**
 * Edits the name/amount of an existing fixed-bill template (screen 9).
 * Only takes the template's id as an argument — the caller (BillsFragment)
 * already holds the full BillEntity and applies the edited values back to
 * it, since this dialog doesn't own a repository reference itself.
 */
public class EditBillDialog extends DialogFragment {

    public interface OnBillEditedListener {
        void onBillEdited(String name, double amount, String bank);
    }

    private static final String ARG_BILL_ID = "arg_bill_id";
    private static final String ARG_CURRENT_NAME = "arg_current_name";
    private static final String ARG_CURRENT_AMOUNT = "arg_current_amount";
    private static final String ARG_CURRENT_BANK = "arg_current_bank";

    private OnBillEditedListener listener;

    public static EditBillDialog newInstance(long billId) {
        EditBillDialog dialog = new EditBillDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_BILL_ID, billId);
        dialog.setArguments(args);
        return dialog;
    }

    public static EditBillDialog newInstance(long billId, String currentName, double currentAmount, String currentBank) {
        EditBillDialog dialog = new EditBillDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_BILL_ID, billId);
        args.putString(ARG_CURRENT_NAME, currentName);
        args.putDouble(ARG_CURRENT_AMOUNT, currentAmount);
        args.putString(ARG_CURRENT_BANK, currentBank);
        dialog.setArguments(args);
        return dialog;
    }

    public static EditBillDialog newInstance(long billId, String currentName, double currentAmount) {
        EditBillDialog dialog = new EditBillDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_BILL_ID, billId);
        args.putString(ARG_CURRENT_NAME, currentName);
        args.putDouble(ARG_CURRENT_AMOUNT, currentAmount);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnBillEditedListener(OnBillEditedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String currentName = args.getString(ARG_CURRENT_NAME, "");
        double currentAmount = args.getDouble(ARG_CURRENT_AMOUNT, 0);
        String currentBank = args.getString(ARG_CURRENT_BANK, Constants.DEFAULT_BILL_BANK);

        View content = getLayoutInflater().inflate(R.layout.dialog_edit_bill, null);
        EditText inputName = content.findViewById(R.id.input_bill_name);
        EditText inputAmount = content.findViewById(R.id.input_bill_amount);
        Spinner inputBank = content.findViewById(R.id.input_bill_bank);
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Constants.BILL_BANKS);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputBank.setAdapter(bankAdapter);
        int bankIndex = java.util.Arrays.asList(Constants.BILL_BANKS).indexOf(currentBank);
        inputBank.setSelection(bankIndex >= 0 ? bankIndex : java.util.Arrays.asList(Constants.BILL_BANKS).indexOf(Constants.DEFAULT_BILL_BANK));
        inputName.setText(currentName);
        if (currentAmount > 0) {
            inputAmount.setText(String.valueOf(currentAmount));
        }

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_edit_bill)
                .setView(content)
                .setPositiveButton(R.string.action_save, (dialogInterface, which) -> {
                    String name = inputName.getText().toString().trim();
                    String rawAmount = inputAmount.getText().toString().trim();

                    if (!ValidationUtils.isNonEmpty(name) || !ValidationUtils.isPositiveAmount(rawAmount)) {
                        return; // TODO: surface inline validation instead of silently ignoring.
                    }

                    if (listener != null) {
                        listener.onBillEdited(name, Double.parseDouble(rawAmount), inputBank.getSelectedItem().toString());
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
