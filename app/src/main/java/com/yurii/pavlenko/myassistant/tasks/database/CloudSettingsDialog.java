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
            android.text.Editable urlEditable = binding.etWebDavUrl.getText();
            android.text.Editable usernameEditable = binding.etUsername.getText();
            android.text.Editable passwordEditable = binding.etPassword.getText();

            String url = urlEditable != null ? urlEditable.toString() : "";
            String username = usernameEditable != null ? usernameEditable.toString() : "";
            String password = passwordEditable != null ? passwordEditable.toString() : "";
            boolean autoBackup = binding.cbAutoBackup.isChecked();

            configManager.saveConfig(url, username, password, autoBackup);
            Toast.makeText(requireContext(), "Cloud settings saved successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Test connection button action
        binding.btnTestConnection.setOnClickListener(v -> {
            android.text.Editable urlEditable = binding.etWebDavUrl.getText();
            android.text.Editable usernameEditable = binding.etUsername.getText();
            android.text.Editable passwordEditable = binding.etPassword.getText();

            String url = urlEditable != null ? urlEditable.toString().trim() : "";
            String username = usernameEditable != null ? usernameEditable.toString().trim() : "";
            String password = passwordEditable != null ? passwordEditable.toString() : "";

            if (url.isEmpty()) {
                binding.etWebDavUrl.setError("URL cannot be empty");
                return;
            }

            Toast.makeText(requireContext(), "Testing connection...", Toast.LENGTH_SHORT).show();

            WebDavTestClient testClient = new WebDavTestClient();
            testClient.testConnection(url, username, password, new WebDavTestClient.TestCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Connection successful!", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            android.text.Editable urlEditable = binding.etWebDavUrl.getText();
            android.text.Editable usernameEditable = binding.etUsername.getText();
            android.text.Editable passwordEditable = binding.etPassword.getText();

            outState.putString("url", urlEditable != null ? urlEditable.toString() : "");
            outState.putString("username", usernameEditable != null ? usernameEditable.toString() : "");
            outState.putString("password", passwordEditable != null ? passwordEditable.toString() : "");
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