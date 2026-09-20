package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.yurii.pavlenko.myassistant.databinding.PopupMenuCustomBinding;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;

public class BackupMenuHandler {

    private static PopupWindow activePopupWindow;

    public interface OnMenuActionListener {
        void onExportLocal();
        void onImportLocal();
        void onCloudSettings();
        void onCloudExport();
        void onCloudImport();
    }

    public static boolean isShowing() {
        return activePopupWindow != null && activePopupWindow.isShowing();
    }

    public static void showCustomPopupMenu(Context context, View anchorView, OnMenuActionListener listener) {
        dismissMenu();

        PopupMenuCustomBinding binding = PopupMenuCustomBinding.inflate(LayoutInflater.from(context));
        View popupView = binding.getRoot();

        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                popupView.getMeasuredWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        float density = context.getResources().getDisplayMetrics().density;
        popupWindow.setElevation(8f * density);

        binding.actionExportDb.setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onExportLocal();
            }
        });

        binding.actionImportDb.setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                showImportConfirmation(context, listener::onImportLocal);
            }
        });

        binding.actionCloudSettings.setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onCloudSettings();
            }
        });

        binding.actionCloudExport.setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onCloudExport();
            }
        });

        binding.actionCloudImport.setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                showImportConfirmation(context, listener::onCloudImport);
            }
        });

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int xOffset = dpToPx(context, 16);
        int yOffset = location[1] + dpToPx(context, 4);

        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.END, xOffset, yOffset);

        activePopupWindow = popupWindow;
    }

    private static void showImportConfirmation(Context context, Runnable importAction) {
        DeleteConfirmationDialog.showCustom(
                context,
                "Attention!",
                "Restoring the database will result in the loss of the current version. Are you sure you want to proceed?",
                "Confirm import",
                importAction
        );
    }

    public static void dismissMenu() {
        if (activePopupWindow != null) {
            if (activePopupWindow.isShowing()) {
                activePopupWindow.dismiss();
            }
            activePopupWindow = null;
        }
    }

    private static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}