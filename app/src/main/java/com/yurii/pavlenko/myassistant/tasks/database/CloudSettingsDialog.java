package com.yurii.pavlenko.myassistant.tasks.database;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
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
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogCloudSettingsBinding;

import java.util.concurrent.TimeUnit;

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
        binding = DialogCloudSettingsBinding.inflate(LayoutInflater.from(requireContext()));
        configManager = new CloudConfigManager(requireContext());

        binding.cbAutoBackup.setTextOn("");
        binding.cbAutoBackup.setTextOff("");

        // Restore state or load from secure storage
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

        setupPasswordVisibilityToggle();
        updateSwitchTextualState(binding.cbAutoBackup, binding.cbAutoBackup.isChecked());

        binding.cbAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateSwitchTextualState(binding.cbAutoBackup, isChecked)
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        binding.btnSaveConfig.setOnClickListener(v -> saveConfiguration());
        binding.btnTestConnection.setOnClickListener(v -> testConnection());

        return dialog;
    }

    private void setupPasswordVisibilityToggle() {
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
    }

    private void saveConfiguration() {
        String url = getTrimmedText(binding.etWebDavUrl);
        String username = getTrimmedText(binding.etUsername);
        String password = getRawText(binding.etPassword);
        boolean autoBackup = binding.cbAutoBackup.isChecked();

        configManager.saveConfig(url, username, password, autoBackup);

        WorkManager workManager = WorkManager.getInstance(requireContext());
        if (autoBackup) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest backupRequest = new PeriodicWorkRequest.Builder(
                    CloudBackupWorker.class, 1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build();

            workManager.enqueueUniquePeriodicWork(
                    "CloudAutoBackupWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    backupRequest
            );
        } else {
            workManager.cancelUniqueWork("CloudAutoBackupWork");
        }

        Toast.makeText(requireContext(), "Cloud settings saved successfully", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void testConnection() {
        String url = getTrimmedText(binding.etWebDavUrl);
        String username = getTrimmedText(binding.etUsername);
        String password = getRawText(binding.etPassword);

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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putString("url", getRawText(binding.etWebDavUrl));
            outState.putString("username", getRawText(binding.etUsername));
            outState.putString("password", getRawText(binding.etPassword));
            outState.putBoolean("auto_backup", binding.cbAutoBackup.isChecked());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getTrimmedText(com.google.android.material.textfield.TextInputEditText editText) {
        Editable editable = editText.getText();
        return editable != null ? editable.toString().trim() : "";
    }

    private String getRawText(com.google.android.material.textfield.TextInputEditText editText) {
        Editable editable = editText.getText();
        return editable != null ? editable.toString() : "";
    }

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