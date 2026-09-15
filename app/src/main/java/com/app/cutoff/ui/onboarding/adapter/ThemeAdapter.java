package com.app.cutoff.ui.onboarding.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.cutoff.R;
import com.app.cutoff.data.preference.ThemePreference;

import java.util.List;

/**
 * Drives the theme picker list (Light / Dark / Sage / Slate) on screen 3.
 * Each row is a preview swatch + label + radio indicator, matching the
 * wireframe's "Choose theme" screen.
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {

    public interface OnThemeSelectedListener {
        void onThemeSelected(String themeMode);
    }

    public static class ThemeOption {
        final String mode;
        final String label;
        final int previewDrawableRes;

        public ThemeOption(String mode, String label, int previewDrawableRes) {
            this.mode = mode;
            this.label = label;
            this.previewDrawableRes = previewDrawableRes;
        }
    }

    private final List<ThemeOption> options;
    private final OnThemeSelectedListener listener;
    private String selectedMode;

    public ThemeAdapter(List<ThemeOption> options, String initiallySelected, OnThemeSelectedListener listener) {
        this.options = options;
        this.selectedMode = initiallySelected != null ? initiallySelected : ThemePreference.MODE_LIGHT;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_option, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        ThemeOption option = options.get(position);
        holder.label.setText(option.label);
        int color;
        switch (option.mode) {
            case ThemePreference.MODE_DARK: color = 0xFF151515; break;
            case ThemePreference.MODE_SAGE: color = 0xFF1B2B22; break;
            case ThemePreference.MODE_SLATE: color = 0xFF1C1F24; break;
            case ThemePreference.MODE_YELLOW: color = 0xFFF4D56A; break;
            default: color = 0xFFFFFFFF;
        }
        android.graphics.drawable.GradientDrawable swatch = new android.graphics.drawable.GradientDrawable();
        swatch.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        swatch.setColor(color);
        swatch.setStroke(1, 0xFF808080);
        holder.preview.setImageDrawable(swatch);
        String visibleSelection = selectedMode;
        if (ThemePreference.MODE_SYSTEM.equals(visibleSelection)) {
            boolean dark = (holder.itemView.getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            visibleSelection = dark ? ThemePreference.MODE_DARK : ThemePreference.MODE_LIGHT;
        }
        holder.radioButton.setChecked(option.mode.equals(visibleSelection));
        holder.itemView.setContentDescription(option.label
                + (option.mode.equals(visibleSelection) ? ", selected" : ""));

        holder.itemView.setOnClickListener(v -> {
            String previousMode = selectedMode;
            selectedMode = option.mode;
            notifyItemChanged(options.indexOf(findOptionByMode(previousMode)));
            notifyItemChanged(position);
            listener.onThemeSelected(option.mode);
        });
    }

    private ThemeOption findOptionByMode(String mode) {
        for (ThemeOption option : options) {
            if (option.mode.equals(mode)) return option;
        }
        return options.get(0);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class ThemeViewHolder extends RecyclerView.ViewHolder {
        final ImageView preview;
        final TextView label;
        final RadioButton radioButton;

        ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.image_theme_preview);
            label = itemView.findViewById(R.id.text_theme_label);
            radioButton = itemView.findViewById(R.id.radio_theme_selected);
        }
    }
}
