package com.yurii.pavlenko.myassistant.tasks.database;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogCloudSettingsBinding;

/**
 * DialogFragment responsible for handling cloud storage settings,
 * lifecycle-safe state preservation during screen rotation, and secure credential entry.
 * Created on: 2026-09-13
 */
public class CloudSettingsDialog extends DialogFragment {

    private DialogCloudSettingsBinding binding;
    private CloudConfigManager configManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate layout using ViewBinding
        binding = DialogCloudSettingsBinding.inflate(LayoutInflater.from(requireContext()));
        configManager = new CloudConfigManager(requireContext());

        // Ensure textOn and textOff are initialized to prevent NullPointerException
        binding.cbAutoBackup.setTextOn("");
        binding.cbAutoBackup.setTextOff("");

        // Restore state after rotation or load from secure storage
        if (savedInstanceState != null) {
            binding.etWebDavUrl.setText(savedInstanceState.getString("url", ""));
            binding.etUsername.setText(savedInstanceState.getString("username", ""));
            binding.etPassword.setText(savedInstanceState.getString("password", ""));
            binding.cbAutoBackup.setChecked(savedInstanceState.getBoolean("auto_backup", false));
        } else {
            binding.etWebDavUrl.setText(configManager.getUrl());
            binding.etUsername.setText(configManager.getUsername());
            binding.etPassword.setText(configManager.getPassword());
            binding.cbAutoBackup.setChecked(configManager.isAutoBackupEnabled());
        }

        // Configure password visibility toggle
        final boolean[] isPasswordVisible = {false};
        binding.tilPassword.setEndIconDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_secure));
        binding.tilPassword.setEndIconOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];
            if (isPasswordVisible[0]) {
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.tilPassword.setEndIconDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_view));
            } else {
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.tilPassword.setEndIconDrawable(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_secure));
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });

        updateSwitchTextualState(binding.cbAutoBackup, binding.cbAutoBackup.isChecked());

        // Handle auto-backup toggle state changes
        binding.cbAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchTextualState(binding.cbAutoBackup, isChecked);
        });

        // Build and return the dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Save button action
        binding.btnSaveConfig.setOnClickListener(v -> {
            String url = binding.etWebDavUrl.getText().toString();
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            boolean autoBackup = binding.cbAutoBackup.isChecked();

            configManager.saveConfig(url, username, password, autoBackup);
            Toast.makeText(requireContext(), "Cloud settings saved successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Test connection button placeholder action
        binding.btnTestConnection.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Test connection feature coming in next step", Toast.LENGTH_SHORT).show();
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putString("url", binding.etWebDavUrl.getText().toString());
            outState.putString("username", binding.etUsername.getText().toString());
            outState.putString("password", binding.etPassword.getText().toString());
            outState.putBoolean("auto_backup", binding.cbAutoBackup.isChecked());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Updates the visual style and text color of the auto-backup switch.
     */
    private static void updateSwitchTextualState(SwitchCompat switchCompat, boolean isChecked) {
        if (isChecked) {
            switchCompat.setTextColor(ContextCompat.getColor(switchCompat.getContext(), android.R.color.holo_green_dark));
            switchCompat.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            switchCompat.setTextColor(ContextCompat.getColor(switchCompat.getContext(), R.color.text_color));
            switchCompat.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
}