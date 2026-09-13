package com.yurii.pavlenko.myassistant.tasks.database;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;

public class CloudSettingsDialog {

    public static void show(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cloud_settings, null);

        EditText etUrl = dialogView.findViewById(R.id.etWebDavUrl);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        SwitchCompat cbAutoBackup = dialogView.findViewById(R.id.cbAutoBackup);
        View btnTest = dialogView.findViewById(R.id.btnTestConnection);
        View btnSave = dialogView.findViewById(R.id.btnSaveConfig);

        CloudConfigManager configManager = new CloudConfigManager(context);

        // Loading previously saved data
        etUrl.setText(configManager.getUrl());
        etUsername.setText(configManager.getUsername());
        etPassword.setText(configManager.getPassword());

        boolean isAutoBackupEnabled = configManager.isAutoBackupEnabled();
        cbAutoBackup.setChecked(isAutoBackupEnabled);

        // Apply initial text style based on loaded state
        updateSwitchTextualState(cbAutoBackup, isAutoBackupEnabled, context);

        // Listen for toggle changes to update text style dynamically
        cbAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchTextualState(cbAutoBackup, isChecked, context);
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSave.setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            boolean autoBackup = cbAutoBackup.isChecked();

            configManager.saveConfig(url, username, password, autoBackup);
            Toast.makeText(context, "Cloud settings saved successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnTest.setOnClickListener(v -> {
            Toast.makeText(context, "Test connection feature coming in next step", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private static void updateSwitchTextualState(SwitchCompat switchCompat, boolean isChecked, Context context) {
        if (isChecked) {
            // Active state: Green color and Bold font
            switchCompat.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            switchCompat.setTypeface(null, Typeface.BOLD);
        } else {
            // Inactive state: Default text color and Normal font
            switchCompat.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            switchCompat.setTypeface(null, Typeface.NORMAL);
        }
    }
}