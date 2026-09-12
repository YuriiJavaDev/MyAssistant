package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.yurii.pavlenko.myassistant.R;

public class BackupMenuHandler {

    public interface OnMenuActionListener {
        void onExportLocal();
        void onImportLocal();
        void onCloudSettings();
        void onCloudExport();
        void onCloudImport();
    }

    public static void showCustomPopupMenu(Context context, View anchorView, OnMenuActionListener listener) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_custom, null);

        // Measure view to get its exact dimensions for precise placement
        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        // Initialize popup window
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                popupView.getMeasuredWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Apply elevation for a nice shadow effect
        float density = context.getResources().getDisplayMetrics().density;
        popupWindow.setElevation(8f * density);

        // Handle local export action
        popupView.findViewById(R.id.action_export_db).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listener != null) {
                listener.onExportLocal();
            }
        });

        // Handle local import action
        popupView.findViewById(R.id.action_import_db).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listener != null) {
                listener.onImportLocal();
            }
        });

        // Handle cloud settings action
        popupView.findViewById(R.id.action_cloud_settings).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listener != null) {
                listener.onCloudSettings();
            }
        });

        // Handle manual cloud export action
        popupView.findViewById(R.id.action_cloud_export).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listener != null) {
                listener.onCloudExport();
            }
        });

        // Handle manual cloud import action
        popupView.findViewById(R.id.action_cloud_import).setOnClickListener(v -> {
            popupWindow.dismiss();
            if (listener != null) {
                listener.onCloudImport();
            }
        });

        // Get anchor view location on screen
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        // X offset: exactly 16dp away from the right edge of the screen
        int xOffset = dpToPx(context, 16);

        // Y offset: align near the top of the anchor view / toolbar, lifting it up nicely
        int yOffset = location[1] + dpToPx(context, 4);

        // Display using absolute screen coordinates aligned to top-right
        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.END, xOffset, yOffset);
    }

    private static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}