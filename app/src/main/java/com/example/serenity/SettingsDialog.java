package com.example.serenity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsDialog {
    private Context context;
    private OnSettingsDialogListener mListener;
    private android.app.AlertDialog alert;

    View v;

    public SettingsDialog(Context context) {
        this.context = context;
        buildPlan();
    }

    private void buildPlan() {
        View dialogView = View.inflate(context, R.layout.settings_dialog, null);
        setView(dialogView);

        Button cancel = dialogView.findViewById(R.id.deleteno);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        // Initialize and build the AlertBuilderDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                .setView(dialogView);
        alert = builder.create();
        if (alert.getWindow() != null)
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void show() {
        alert.show();
    }

    private void setView(View view) {
        v = view;
    }

    public View getView() {
       return v;
    }


    void setOnSettingsDialogListener(OnSettingsDialogListener listener) {
        mListener = listener;
    }

    interface OnSettingsDialogListener {
    }
    static class Builder {

        private final Context mContext;
        private OnSettingsDialogListener mListener;

        static Builder instance(Context context) {
            return new Builder(context);
        }

        Builder(Context context) {
            mContext = context;
        }

        Builder setOnColorSelectedListener(OnSettingsDialogListener listener) {
            mListener = listener;
            return this;
        }

        SettingsDialog create() {
            SettingsDialog dialog = new SettingsDialog(mContext);
            dialog.setOnSettingsDialogListener(mListener);
            return dialog;
        }

        /*public Builder setSelectedColor(int color) {
            mColor = color;
            return this;
        }*/
    }
}