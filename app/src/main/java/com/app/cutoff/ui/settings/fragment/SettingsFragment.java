package com.app.cutoff.ui.settings.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;
import com.app.cutoff.data.database.entity.SalaryEntity;
import com.app.cutoff.data.preference.ThemePreference;
import com.app.cutoff.ui.settings.viewmodel.SettingsViewModel;
import com.app.cutoff.utils.CurrencyUtils;
import com.app.cutoff.utils.ThemeManager;
import com.app.cutoff.utils.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen 8: Settings. Shows current theme (tap to change — a simple
 * picker dialog reusing the same theme options as onboarding), a link
 * into Fixed Bills management (screen 9), a read-only salary summary
 * with a link to edit it, and export/import of the whole database as a
 * JSON backup file.
 */
@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // Registered as fields (not inside onViewCreated) so they're set up
    // before the Fragment reaches STARTED, per the ActivityResultLauncher
    // contract -- see AndroidX docs on registerForActivityResult().
    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) viewModel.exportData(uri);
            });

    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) confirmAndImport(uri);
            });

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        TextView themeValue = view.findViewById(R.id.text_theme_value);
        TextView fixedBillsCount = view.findViewById(R.id.text_fixed_bills_count);
        TextView firstHalfValue = view.findViewById(R.id.text_salary_first_half);
        TextView secondHalfValue = view.findViewById(R.id.text_salary_second_half);

        themeValue.setText(readableThemeLabel(viewModel.getThemeMode()));

        // Keep the latest salary around so the edit dialog can be pre-filled with it.
        final SalaryEntity[] latestSalary = {null};
        viewModel.getSalary().observe(getViewLifecycleOwner(), salary -> {
            latestSalary[0] = salary;
            firstHalfValue.setText(CurrencyUtils.format(salary == null ? 0 : salary.getFirstToFifteenth()));
            secondHalfValue.setText(CurrencyUtils.format(salary == null ? 0 : salary.getSixteenthToEnd()));
        });

        viewModel.getFixedBillCount().observe(getViewLifecycleOwner(), count ->
                fixedBillsCount.setText((count != null ? count : 0) + " templates · Manage"));

        view.findViewById(R.id.row_theme).setOnClickListener(v -> showThemePicker(themeValue));

        view.findViewById(R.id.row_fixed_bills).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_fixedBills));

        view.findViewById(R.id.row_salary).setOnClickListener(v -> showEditSalaryDialog(latestSalary[0]));

        view.findViewById(R.id.row_export_data).setOnClickListener(v ->
                exportLauncher.launch(suggestedBackupFileName()));

        view.findViewById(R.id.row_import_data).setOnClickListener(v ->
                importLauncher.launch(new String[]{"application/json"}));

        viewModel.getBackupStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmAndImport(Uri source) {
        new com.app.cutoff.ui.common.CutoffSheetBuilder(requireContext())
                .setTitle(R.string.title_confirm_import)
                .setMessage(R.string.message_confirm_import)
                .setPositiveButton(R.string.action_import, (dialogInterface, which) ->
                        viewModel.importData(source))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private String suggestedBackupFileName() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        return "cutoff_backup_" + date + ".json";
    }

    private void showEditSalaryDialog(@Nullable SalaryEntity currentSalary) {
        View content = getLayoutInflater().inflate(R.layout.dialog_edit_salary, null);
        EditText inputFirstHalf = content.findViewById(R.id.input_first_to_fifteenth);
        EditText inputSecondHalf = content.findViewById(R.id.input_sixteenth_to_end);

        if (currentSalary != null) {
            inputFirstHalf.setText(String.valueOf(currentSalary.getFirstToFifteenth()));
            inputSecondHalf.setText(String.valueOf(currentSalary.getSixteenthToEnd()));
        }

        new com.app.cutoff.ui.common.CutoffSheetBuilder(requireContext())
                .setTitle(R.string.title_edit_salary)
                .setView(content)
                .setPositiveButton(R.string.action_save, (dialogInterface, which) -> {
                    String rawFirst = inputFirstHalf.getText().toString().trim();
                    String rawSecond = inputSecondHalf.getText().toString().trim();

                    if (!ValidationUtils.isPositiveAmount(rawFirst) || !ValidationUtils.isPositiveAmount(rawSecond)) {
                        return; // TODO: surface inline validation instead of silently ignoring.
                    }
                    viewModel.updateSalary(rawFirst, rawSecond);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showThemePicker(TextView themeValue) {
        String[] labels = {"Light", "Dark", "Sage", "Slate", "Yellow"};
        String[] modes = {
                ThemePreference.MODE_LIGHT, ThemePreference.MODE_DARK,
                ThemePreference.MODE_SAGE, ThemePreference.MODE_SLATE, ThemePreference.MODE_YELLOW
        };

        String[] descriptions = {"White background · Black text", "Charcoal background · White text",
                "Dark sage background · White text", "Blue-gray background · White text",
                "Soft yellow background · Dark text"};
        int[] backgrounds = {0xFFFFFFFF, 0xFF151515, 0xFF1B2B22, 0xFF1C1F24, 0xFFFFF8DE};
        android.widget.ArrayAdapter<String> options = new android.widget.ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_list_item_1, labels) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull android.view.ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                float density = getResources().getDisplayMetrics().density;
                row.setSingleLine(false);
                row.setText(labels[position] + (modes[position].equals(viewModel.getThemeMode()) ? "  ✓" : "")
                        + "\n" + descriptions[position]);
                row.setTextSize(14);
                row.setTextColor(position == 0 || position == 4 ? 0xFF292511 : 0xFFFFFFFF);
                android.graphics.drawable.GradientDrawable swatch = new android.graphics.drawable.GradientDrawable();
                swatch.setColor(backgrounds[position]);
                swatch.setCornerRadius(14 * density);
                row.setBackground(swatch);
                row.setPadding((int) (24 * density), (int) (16 * density),
                        (int) (24 * density), (int) (16 * density));
                row.setMinHeight((int) (76 * density));
                return row;
            }
        };

        new com.app.cutoff.ui.common.CutoffSheetBuilder(requireContext())
                .setTitle(R.string.title_choose_theme)
                .setAdapter(options, (dialogInterface, which) -> {
                    boolean changed = viewModel.setThemeMode(modes[which]);
                    themeValue.setText(labels[which]);
                    if (changed) {
                        // Must happen before recreate(): setTheme() alone isn't enough to
                        // switch a DayNight theme (Light/Dark) — AppCompatDelegate's night
                        // mode also needs to be updated, or recreate() would still resolve
                        // against the stale mode left over from app launch.
                        ThemeManager.applyNightMode(modes[which]);
                        requireActivity().recreate();
                    }
                })
                .show();
    }

    private String readableThemeLabel(String mode) {
        if (ThemePreference.MODE_DARK.equals(mode)) return "Dark";
        if (ThemePreference.MODE_SAGE.equals(mode)) return "Sage";
        if (ThemePreference.MODE_SLATE.equals(mode)) return "Slate";
        if (ThemePreference.MODE_YELLOW.equals(mode)) return "Yellow";
        if (ThemePreference.MODE_LIGHT.equals(mode)) return "Light";
        return "System";
    }
}
