package com.app.cutoff.ui.cutoff.dialog;

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
 * Dialog for adding an incentive (e.g. "Retro", "Bonus") to a cutoff.
 * Kept as its own class rather than reusing AddBillDialog's Mode enum
 * since incentives have different copy and always add to salary rather
 * than subtract from it — mixing the two would make AddBillDialog's
 * validation/labels branch on a third mode for no shared benefit.
 */
public class AddIncentiveDialog extends DialogFragment {

    public interface OnIncentiveAddedListener {
        void onIncentiveAdded(String name, double amount);
    }

    private OnIncentiveAddedListener listener;

    public static AddIncentiveDialog newInstance() {
        return new AddIncentiveDialog();
    }

    public void setOnIncentiveAddedListener(OnIncentiveAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View content = getLayoutInflater().inflate(R.layout.dialog_add_incentive, null);
        EditText inputName = content.findViewById(R.id.input_incentive_name);
        EditText inputAmount = content.findViewById(R.id.input_incentive_amount);

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_add_incentive)
                .setView(content)
                .setPositiveButton(R.string.action_add, (dialogInterface, which) -> {
                    String name = inputName.getText().toString().trim();
                    String rawAmount = inputAmount.getText().toString().trim();

                    if (!ValidationUtils.isNonEmpty(name) || !ValidationUtils.isPositiveAmount(rawAmount)) {
                        return; // TODO: surface inline validation instead of silently ignoring.
                    }

                    if (listener != null) {
                        listener.onIncentiveAdded(name, Double.parseDouble(rawAmount));
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
