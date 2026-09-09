package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

public class MainActionsHandler {

    public static void triggerExportDatabase(ActivityResultLauncher<Intent> exportLauncher) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "task_database.db");
        exportLauncher.launch(intent);
    }

    public static void triggerImportDatabase(ActivityResultLauncher<Intent> importLauncher) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importLauncher.launch(intent);
    }
}