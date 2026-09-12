package com.yurii.pavlenko.myassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;
import com.yurii.pavlenko.myassistant.tasks.database.BackupMenuHandler;
import com.yurii.pavlenko.myassistant.tasks.database.DatabaseBackupManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String[] allTitles = new String[]{"Tasks", "Weather", "Currency", "Calc", "Scan", "Steps", "Flights"};
    private final String[] firstRowTitles = new String[]{"Tasks", "Weather", "Currency", "Calc"};
    private final String[] secondRowTitles = new String[]{"Scan", "Steps", "Flights"};
    private boolean isSyncing = false;

    // Launcher for importing database file
    private final ActivityResultLauncher<String> importDatabaseLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    boolean success = DatabaseBackupManager.importDatabase(this, uri);
                    if (success) {
                        Toast.makeText(this, "Database imported successfully. Restarting...", Toast.LENGTH_LONG).show();

                        // Process pull-restart: clears the SQLite cache and launches the application from a clean slate.
                        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            System.exit(0);
                        }
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

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        boolean isTwoRows = binding.tabLayoutSecond != null;

        if (isTwoRows) {
            for (String title : firstRowTitles) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
            for (String title : secondRowTitles) {
                binding.tabLayoutSecond.addTab(binding.tabLayoutSecond.newTab().setText(title));
            }
        } else {
            for (String title : allTitles) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
        }

        if (isTwoRows) {
            // --- PORTRAIT MODE: Two rows logic ---
            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (isSyncing) return;
                    int position = tab.getPosition();
                    if (binding.viewPager.getCurrentItem() != position) {
                        binding.viewPager.setCurrentItem(position);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });

            binding.tabLayoutSecond.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (isSyncing) return;
                    int position = tab.getPosition() + 4;
                    if (binding.viewPager.getCurrentItem() != position) {
                        binding.viewPager.setCurrentItem(position);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });

            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    isSyncing = true;

                    int primaryColor = getResources().getColor(R.color.colorPrimary);
                    int backgroundColor = getResources().getColor(R.color.background);

                    if (position < 4) {
                        if (binding.tabLayout.getSelectedTabPosition() != position) {
                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
                        }
                        binding.tabLayoutSecond.selectTab(null);
                        binding.tabLayout.setSelectedTabIndicatorColor(primaryColor);
                        binding.tabLayoutSecond.setSelectedTabIndicatorColor(backgroundColor);
                    } else {
                        int secondRowPos = position - 4;
                        if (binding.tabLayoutSecond.getSelectedTabPosition() != secondRowPos) {
                            binding.tabLayoutSecond.selectTab(binding.tabLayoutSecond.getTabAt(secondRowPos));
                        }
                        binding.tabLayout.selectTab(null);
                        binding.tabLayout.setSelectedTabIndicatorColor(backgroundColor);
                        binding.tabLayoutSecond.setSelectedTabIndicatorColor(primaryColor);
                    }

                    for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                        if (tab != null && tab.view != null) {
                            tab.view.setSelected(position < 4 && i == position);
                        }
                    }
                    for (int i = 0; i < binding.tabLayoutSecond.getTabCount(); i++) {
                        TabLayout.Tab tab = binding.tabLayoutSecond.getTabAt(i);
                        if (tab != null && tab.view != null) {
                            tab.view.setSelected(position >= 4 && i == (position - 4));
                        }
                    }

                    disableAllCaps(binding.tabLayout);
                    disableAllCaps(binding.tabLayoutSecond);

                    isSyncing = false;
                }
            });

            disableAllCaps(binding.tabLayout);
            disableAllCaps(binding.tabLayoutSecond);

        } else {
            // --- LANDSCAPE MODE: Single row logic ---
            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (isSyncing) return;
                    int position = tab.getPosition();
                    if (binding.viewPager.getCurrentItem() != position) {
                        binding.viewPager.setCurrentItem(position);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });

            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    isSyncing = true;

                    if (binding.tabLayout.getSelectedTabPosition() != position) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
                    }

                    for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                        if (tab != null && tab.view != null) {
                            tab.view.setSelected(i == position);
                        }
                    }

                    disableAllCaps(binding.tabLayout);

                    isSyncing = false;
                }
            });

            disableAllCaps(binding.tabLayout);
        }
    }

    private void disableAllCaps(TabLayout tabLayout) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.view != null) {
                applyNormalCaseToView(tab.view);
            }
        }
    }

    private void applyNormalCaseToView(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setAllCaps(false);
            tv.setTransformationMethod(null);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyNormalCaseToView(group.getChildAt(i));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_overflow_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_custom_overflow);
        if (menuItem != null) {
            View actionView = menuItem.getActionView();
            if (actionView != null) {
                actionView.setOnClickListener(v -> {
                    BackupMenuHandler.showCustomPopupMenu(this, v, new BackupMenuHandler.OnMenuActionListener() {
                        @Override
                        public void onExportLocal() {
                            Intent shareIntent = DatabaseBackupManager.getExportShareIntent(MainActivity.this);
                            if (shareIntent != null) {
                                startActivity(shareIntent);
                            } else {
                                Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onImportLocal() {
                            if (importDatabaseLauncher != null) {
                                importDatabaseLauncher.launch("*/*");
                            }
                        }

                        @Override
                        public void onCloudSettings() {
                            Toast.makeText(MainActivity.this, "Cloud Settings coming soon", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCloudExport() {
                            Toast.makeText(MainActivity.this, "Save to Cloud coming soon", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCloudImport() {
                            Toast.makeText(MainActivity.this, "Restore from Cloud coming soon", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_custom_overflow) {
            // Find the anchor view for the menu (toolbar or the button itself)
            View anchorView = findViewById(R.id.action_custom_overflow);
            if (anchorView == null) {
                anchorView = binding.toolbar;
            }

            BackupMenuHandler.showCustomPopupMenu(this, anchorView, new BackupMenuHandler.OnMenuActionListener() {
                @Override
                public void onExportLocal() {
                    Intent shareIntent = DatabaseBackupManager.getExportShareIntent(MainActivity.this);
                    if (shareIntent != null) {
                        startActivity(shareIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onImportLocal() {
                    if (importDatabaseLauncher != null) {
                        importDatabaseLauncher.launch("*/*");
                    }
                }

                @Override
                public void onCloudSettings() {
                    Toast.makeText(MainActivity.this, "Cloud Settings coming soon", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCloudExport() {
                    Toast.makeText(MainActivity.this, "Save to Cloud coming soon", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCloudImport() {
                    Toast.makeText(MainActivity.this, "Restore from Cloud coming soon", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}