package com.yurii.pavlenko.myassistant.backup.ui;

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

import com.yurii.pavlenko.myassistant.backup.BackupFailure;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogCloudSettingsBinding;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

import java.util.concurrent.TimeUnit;
import com.yurii.pavlenko.myassistant.backup.CloudBackupWorker;
import com.yurii.pavlenko.myassistant.backup.CloudConfigManager;
import com.yurii.pavlenko.myassistant.backup.WebDavTestClient;
import com.yurii.pavlenko.myassistant.backup.WebDavUtils;

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

        // The password is never kept in the saved state, so it is always read back from secure storage.
        binding.etPassword.setText(configManager.getPassword());
        if (savedInstanceState != null) {
            binding.etWebDavUrl.setText(savedInstanceState.getString("url", ""));
            binding.etUsername.setText(savedInstanceState.getString("username", ""));
            binding.cbAutoBackup.setChecked(savedInstanceState.getBoolean("auto_backup", false));
        } else {
            binding.etWebDavUrl.setText(configManager.getUrl());
            binding.etUsername.setText(configManager.getUsername());
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

        if (!WebDavUtils.isSecureUrl(url)) {
            binding.etWebDavUrl.setError(getString(R.string.backup_error_insecure_url));
            return;
        }
        if (!configManager.saveConfig(url, username, password, autoBackup)) {
            Toast.makeText(requireContext(), R.string.backup_cloud_secure_storage_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

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

        Toast.makeText(requireContext(), R.string.backup_cloud_settings_saved, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void testConnection() {
        String url = getTrimmedText(binding.etWebDavUrl);
        String username = getTrimmedText(binding.etUsername);
        String password = getRawText(binding.etPassword);

        if (url.isEmpty()) {
            binding.etWebDavUrl.setError(getString(R.string.backup_cloud_url_empty));
            return;
        }
        if (!WebDavUtils.isSecureUrl(url)) {
            binding.etWebDavUrl.setError(getString(R.string.backup_error_insecure_url));
            return;
        }

        Toast.makeText(requireContext(), R.string.backup_cloud_testing, Toast.LENGTH_SHORT).show();

        WebDavTestClient testClient = new WebDavTestClient();
        testClient.testConnection(url, username, password, new WebDavTestClient.TestCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), R.string.backup_cloud_connection_successful, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(BackupFailure failure) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), BackupMessages.failure(requireContext(), failure), Toast.LENGTH_LONG).show()
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
            switchCompat.setTextColor(ThemeColors.get(switchCompat.getContext(), R.attr.maOk));
            switchCompat.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            switchCompat.setTextColor(ThemeColors.get(switchCompat.getContext(), R.attr.maText));
            switchCompat.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
}