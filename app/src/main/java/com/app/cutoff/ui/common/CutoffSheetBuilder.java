package com.app.cutoff.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.app.cutoff.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

/** Shared, scrollable sheet for forms, choices and confirmations. */
public final class CutoffSheetBuilder {
    private final Context context;
    private CharSequence title, message, positive, negative;
    private View content;
    private ListAdapter adapter;
    private DialogInterface.OnClickListener onPositive, onNegative, onChoice;

    public CutoffSheetBuilder(Context context) { this.context = context; }
    public CutoffSheetBuilder setTitle(int res) { return setTitle(context.getText(res)); }
    public CutoffSheetBuilder setTitle(CharSequence value) { title = value; return this; }
    public CutoffSheetBuilder setMessage(int res) { message = context.getText(res); return this; }
    public CutoffSheetBuilder setView(View view) { content = view; return this; }
    public CutoffSheetBuilder setPositiveButton(int res, DialogInterface.OnClickListener listener) {
        positive = context.getText(res); onPositive = listener; return this;
    }
    public CutoffSheetBuilder setNegativeButton(int res, DialogInterface.OnClickListener listener) {
        negative = context.getText(res); onNegative = listener; return this;
    }
    public CutoffSheetBuilder setAdapter(ListAdapter value, DialogInterface.OnClickListener listener) {
        adapter = value; onChoice = listener; return this;
    }

    private int dp(int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private TextView text(CharSequence value, int size) {
        TextView view = new TextView(context);
        view.setText(value); view.setTextSize(size);
        view.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0));
        return view;
    }

    public Dialog create() {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(12), dp(24), dp(24));
        GradientDrawable background = new GradientDrawable();
        background.setColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, 0));
        background.setCornerRadii(new float[]{dp(24),dp(24),dp(24),dp(24),0,0,0,0});
        panel.setBackground(background);
        View handle = new View(context);
        GradientDrawable handleShape = new GradientDrawable();
        handleShape.setColor(MaterialColors.getColor(context, R.attr.cutoffOutline, 0));
        handleShape.setCornerRadius(dp(3)); handle.setBackground(handleShape);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(36),dp(4));
        handleParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        handleParams.bottomMargin = dp(24); panel.addView(handle,handleParams);
        TextView heading = text(title,24);
        androidx.core.view.ViewCompat.setAccessibilityHeading(heading, true);
        panel.addView(heading);
        if (message != null) {
            TextView description = text(message,14); description.setPadding(0,dp(12),0,dp(8)); panel.addView(description);
        }
        if (content != null) {
            content.setPadding(0,dp(20),0,dp(8)); panel.addView(content);
        }
        if (adapter != null) {
            for (int i=0; i<adapter.getCount(); i++) {
                final int index=i;
                View row=adapter.getView(i,null,panel);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1,-2);
                rowParams.topMargin=dp(12);
                row.setOnClickListener(v -> { if(onChoice!=null) onChoice.onClick(dialog,index); dialog.dismiss(); });
                row.setFocusable(true); panel.addView(row,rowParams);
            }
        }
        if (positive != null) {
            MaterialButton save = new MaterialButton(context);
            save.setText(positive); save.setCornerRadius(dp(14));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2);
            params.topMargin=dp(16); panel.addView(save,params);
            save.setOnClickListener(v -> {
                if(content!=null && !validate(content)) return;
                if(onPositive!=null) onPositive.onClick(dialog,DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            });
        }
        if (negative != null) {
            MaterialButton cancel = new MaterialButton(context,null,com.google.android.material.R.attr.materialButtonOutlinedStyle);
            cancel.setText(negative); panel.addView(cancel,new LinearLayout.LayoutParams(-1,-2));
            cancel.setOnClickListener(v -> { if(onNegative!=null) onNegative.onClick(dialog,DialogInterface.BUTTON_NEGATIVE); dialog.dismiss(); });
        }
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(false); scroll.addView(panel);
        dialog.setContentView(scroll);
        dialog.setOnShowListener(ignored -> {
            View sheet=dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if(sheet!=null) {
                sheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                BottomSheetBehavior<View> behavior=BottomSheetBehavior.from(sheet);
                behavior.setSkipCollapsed(true); behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        if(dialog.getWindow()!=null) dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }
    public Dialog show() { Dialog dialog=create(); dialog.show(); return dialog; }

    private boolean validate(View view) {
        if(view instanceof EditText) {
            EditText field=(EditText)view;
            String value=field.getText().toString().trim();
            boolean numeric=(field.getInputType() & InputType.TYPE_MASK_CLASS)==InputType.TYPE_CLASS_NUMBER;
            boolean valid=!value.isEmpty();
            if(valid && numeric) {
                try { double amount=Double.parseDouble(value); valid=amount>0 && !Double.isInfinite(amount) && !Double.isNaN(amount); }
                catch(NumberFormatException e) { valid=false; }
            }
            if(!valid) { field.setError(numeric ? "Enter an amount greater than zero" : "Enter a name"); field.requestFocus(); return false; }
            field.setError(null);
        }
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++) if(!validate(group.getChildAt(i))) return false;
        }
        return true;
    }
}
