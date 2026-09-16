package com.yurii.pavlenko.myassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;
import com.yurii.pavlenko.myassistant.tasks.database.BackupMenuHandler;
import com.yurii.pavlenko.myassistant.tasks.database.DatabaseBackupManager;
import com.yurii.pavlenko.myassistant.tasks.database.MainMenuActionsHandler;
import com.yurii.pavlenko.myassistant.ui.navigation.ViewPagerConfigurator;

/**
 * Main activity responsible for managing tab navigation via ViewPager2,
 * and coordinating backup/settings overflow menu actions.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String STATE_MENU_OPEN = "state_menu_open";

    // Launcher for importing database file
    private final ActivityResultLauncher<String> importDatabaseLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    boolean success = DatabaseBackupManager.importDatabase(this, uri);
                    if (success) {
                        Toast.makeText(this, "Database imported successfully. Restarting...", Toast.LENGTH_LONG).show();
                        restartApp();
                    } else {
                        Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ViewPagerConfigurator.configure(this, binding);
        restoreMenuStateIfNeeded(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_MENU_OPEN, BackupMenuHandler.isShowing());
    }

    /**
     * Restores custom overflow menu state after configuration changes if it was open.
     */
    private void restoreMenuStateIfNeeded(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_MENU_OPEN, false)) {
            binding.toolbar.post(() -> showPopupMenu(getMenuAnchorView()));
        }
    }

    /**
     * Resolves the anchor view for the overflow menu, defaulting to toolbar if action view is missing.
     */
    private View getMenuAnchorView() {
        View anchorView = binding.toolbar.findViewById(R.id.action_custom_overflow);
        return anchorView != null ? anchorView : binding.toolbar;
    }

    /**
     * Restarts the application cleanly to apply database changes.
     */
    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            System.exit(0);
        }
    }

    /**
     * Displays the custom backup and settings popup menu via MainMenuActionsHandler.
     */
    private void showPopupMenu(View anchorView) {
        MainMenuActionsHandler.showPopupMenu(
                this,
                anchorView,
                getSupportFragmentManager(),
                importDatabaseLauncher,
                this::restartApp
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_overflow_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_custom_overflow);
        if (menuItem != null) {
            View actionView = menuItem.getActionView();
            if (actionView != null) {
                actionView.setOnClickListener(this::showPopupMenu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_custom_overflow) {
            showPopupMenu(getMenuAnchorView());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}