package com.app.cutoff.ui.bills.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.app.cutoff.R;
import com.app.cutoff.utils.ValidationUtils;

/**
 * Reusable "add bill" bottom sheet/dialog. Used from:
 *  - FixedBillsFragment (onboarding) and ui.bills for FIXED_BILL_TEMPLATE
 *  - CutoffDetailFragment's "Add variable bill" button for VARIABLE_BILL
 *
 * AddIncentiveDialog extends the same layout/validation but is a distinct
 * class (see AddIncentiveDialog) since incentives don't have an icon
 * picker and use different copy ("Add incentive" vs "Add fixed bill").
 */
public class AddBillDialog extends DialogFragment {

    public enum Mode {FIXED_BILL_TEMPLATE, FIXED_BILL, VARIABLE_BILL}

    public interface OnBillAddedListener {
        void onBillAdded(String name, double amount, @Nullable String iconKey);
    }

    private static final String ARG_MODE = "arg_mode";

    private OnBillAddedListener listener;

    public static AddBillDialog newInstance(Mode mode) {
        AddBillDialog dialog = new AddBillDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode.name());
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnBillAddedListener(OnBillAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Mode mode = Mode.valueOf(requireArguments().getString(ARG_MODE, Mode.VARIABLE_BILL.name()));

        View content = getLayoutInflater().inflate(R.layout.dialog_add_bill, null);
        EditText inputName = content.findViewById(R.id.input_bill_name);
        EditText inputAmount = content.findViewById(R.id.input_bill_amount);

        int titleRes = (mode == Mode.FIXED_BILL_TEMPLATE || mode == Mode.FIXED_BILL)
                ? R.string.title_add_fixed_bill
                : R.string.title_add_variable_bill;

        return new AlertDialog.Builder(requireContext())
                .setTitle(titleRes)
                .setView(content)
                .setPositiveButton(R.string.action_add, (dialogInterface, which) -> {
                    String name = inputName.getText().toString().trim();
                    String rawAmount = inputAmount.getText().toString().trim();

                    if (!ValidationUtils.isNonEmpty(name) || !ValidationUtils.isPositiveAmount(rawAmount)) {
                        return; // TODO: surface inline validation instead of silently ignoring.
                    }

                    if (listener != null) {
                        listener.onBillAdded(name, Double.parseDouble(rawAmount), /*iconKey=*/ null);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
