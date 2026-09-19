package com.app.cutoff.ui.bills.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.app.cutoff.utils.Constants;
import com.app.cutoff.utils.BankPreferences;

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
        void onBillEdited(String name, double amount, String bank, String paymentStatus, String recurrenceSchedule);
    }

    private static final String ARG_BILL_ID = "arg_bill_id";
    private static final String ARG_CURRENT_NAME = "arg_current_name";
    private static final String ARG_CURRENT_AMOUNT = "arg_current_amount";
    private static final String ARG_CURRENT_BANK = "arg_current_bank";
    private static final String ARG_CUTOFF_ONLY = "arg_cutoff_only";
    private static final String ARG_CURRENT_STATUS = "arg_current_status";
    private static final String ARG_CURRENT_RECURRENCE = "arg_current_recurrence";

    public void setCutoffOnly(boolean cutoffOnly) {
        requireArguments().putBoolean(ARG_CUTOFF_ONLY, cutoffOnly);
    }

    private OnBillEditedListener listener;

    public static EditBillDialog newInstance(long billId) {
        EditBillDialog dialog = new EditBillDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_BILL_ID, billId);
        dialog.setArguments(args);
        return dialog;
    }

    public static EditBillDialog newInstance(long billId, String currentName, double currentAmount, String currentBank) {
        return newInstance(billId, currentName, currentAmount, currentBank, com.app.cutoff.data.database.entity.BillEntity.PAYMENT_STATUS_PENDING);
    }
    public static EditBillDialog newTemplateInstance(long billId, String currentName, double currentAmount,
                                                     String currentBank, String currentRecurrence) {
        EditBillDialog dialog = newInstance(billId, currentName, currentAmount, currentBank);
        dialog.requireArguments().putString(ARG_CURRENT_RECURRENCE, currentRecurrence);
        return dialog;
    }
    public static EditBillDialog newInstance(long billId, String currentName, double currentAmount, String currentBank, String currentStatus) {
        EditBillDialog dialog = new EditBillDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_BILL_ID, billId);
        args.putString(ARG_CURRENT_NAME, currentName);
        args.putDouble(ARG_CURRENT_AMOUNT, currentAmount);
        args.putString(ARG_CURRENT_BANK, currentBank);
        args.putString(ARG_CURRENT_STATUS, currentStatus);
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
        Spinner inputStatus = content.findViewById(R.id.input_bill_payment_status);
        Spinner inputSchedule = content.findViewById(R.id.input_bill_recurrence);
        java.util.List<String> banks = BankPreferences.getActive(requireContext());
        if (!banks.contains(currentBank)) banks.add(0, currentBank);
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, banks);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputBank.setAdapter(bankAdapter);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Paid", "Pending"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputStatus.setAdapter(statusAdapter);
        inputStatus.setSelection("PAID".equalsIgnoreCase(args.getString(ARG_CURRENT_STATUS, "PENDING")) ? 0 : 1);
        inputBank.setSelection(banks.indexOf(currentBank));
        boolean cutoffOnly = args.getBoolean(ARG_CUTOFF_ONLY);
        if (cutoffOnly) {
            content.findViewById(R.id.label_bill_recurrence).setVisibility(View.GONE);
            inputSchedule.setVisibility(View.GONE);
        } else {
            ArrayAdapter<String> scheduleAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"Every cutoff", "15th cutoff", "30th / month-end cutoff"});
            scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            inputSchedule.setAdapter(scheduleAdapter);
            String recurrence = args.getString(ARG_CURRENT_RECURRENCE,
                    com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_BOTH_CUTOFFS);
            inputSchedule.setSelection(com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_FIRST_CUTOFF.equals(recurrence) ? 1
                    : com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_SECOND_CUTOFF.equals(recurrence) ? 2 : 0);
        }
        inputName.setText(currentName);
        if (currentAmount > 0) {
            inputAmount.setText(String.valueOf(currentAmount));
        }

        return new com.app.cutoff.ui.common.CutoffSheetBuilder(requireContext())
                .setTitle(args.getBoolean(ARG_CUTOFF_ONLY) ? "Edit bill · this cutoff only" : "Edit bill template")
                .setView(content)
                .setPositiveButton(R.string.action_save, (dialogInterface, which) -> {
                    String name = inputName.getText().toString().trim();
                    String rawAmount = inputAmount.getText().toString().trim();

                    if (!ValidationUtils.isNonEmpty(name) || !ValidationUtils.isPositiveAmount(rawAmount)) {
                        return; // TODO: surface inline validation instead of silently ignoring.
                    }

                    if (listener != null) {
                        String schedule = inputSchedule.getSelectedItemPosition() == 1
                                ? com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_FIRST_CUTOFF
                                : inputSchedule.getSelectedItemPosition() == 2
                                ? com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_SECOND_CUTOFF
                                : com.app.cutoff.data.database.entity.BillEntity.RECURRENCE_BOTH_CUTOFFS;
                        listener.onBillEdited(name, Double.parseDouble(rawAmount), inputBank.getSelectedItem().toString(),
                                inputStatus.getSelectedItem().toString().toUpperCase(), schedule);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
