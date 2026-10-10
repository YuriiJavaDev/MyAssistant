package com.yurii.pavlenko.myassistant;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;
import com.yurii.pavlenko.myassistant.tasks.database.BackupMenuHandler;
import com.yurii.pavlenko.myassistant.tasks.database.DatabaseBackupManager;
import com.yurii.pavlenko.myassistant.tasks.database.MainMenuActionsHandler;
import com.yurii.pavlenko.myassistant.tasks.notifications.OverdueAlerts;
import com.yurii.pavlenko.myassistant.tasks.ui.TasksFragment;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskAlarmRescheduler;
import com.yurii.pavlenko.myassistant.ui.navigation.ViewPagerConfigurator;

/**
 * Main activity responsible for managing tab navigation via ViewPager2,
 * and coordinating backup/settings overflow menu actions.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String STATE_MENU_OPEN = "state_menu_open";
    private static final String EXTRA_SHOW_DUE_REMINDERS = "extra_show_due_reminders";
    private static final int TASKS_TAB_POSITION = 0;

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

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications are disabled: reminders will not be shown", Toast.LENGTH_LONG).show();
                }
            }
    );

    /** Creates an intent that opens the task list filtered by reminders whose time has come. */
    public static Intent createShowDueRemindersIntent(Context context) {
        return new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_SHOW_DUE_REMINDERS, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLockScreenVisibility(getIntent());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ViewPagerConfigurator.configure(this, binding);
        restoreMenuStateIfNeeded(savedInstanceState);

        requestNotificationPermissionIfNeeded();
        if (savedInstanceState == null) {
            TaskAlarmRescheduler.rescheduleAsync(this, false, null);
            showDueRemindersIfRequested(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyLockScreenVisibility(intent);
        showDueRemindersIfRequested(intent);
    }

    /**
     * The manifest allows showing over the lock screen so a notification tap skips the unlock prompt;
     * at runtime it stays enabled only for due-reminder launches, never for ordinary ones.
     */
    private void applyLockScreenVisibility(Intent intent) {
        boolean showOverLockScreen = intent.getBooleanExtra(EXTRA_SHOW_DUE_REMINDERS, false);
        setShowWhenLocked(showOverLockScreen);
        setTurnScreenOn(showOverLockScreen);
    }

    /** Acknowledges the overdue alert and shows the tasks it is about. */
    private void showDueRemindersIfRequested(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_DUE_REMINDERS, false)) {
            OverdueAlerts.stop(this);
            binding.viewPager.setCurrentItem(TASKS_TAB_POSITION, false);
            getSupportFragmentManager().setFragmentResult(TasksFragment.REQUEST_SHOW_DUE_REMINDERS, Bundle.EMPTY);
        }
    }

    /** Without this permission (Android 13+) the system silently drops every reminder notification. */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
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
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            new SettingsBottomSheet().show(getSupportFragmentManager(), "SettingsBottomSheet");
            return true;
        } else if (itemId == R.id.action_custom_overflow) {
            showPopupMenu(getMenuAnchorView());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}