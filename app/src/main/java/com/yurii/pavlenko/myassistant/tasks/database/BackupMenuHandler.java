package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.yurii.pavlenko.myassistant.R;

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

        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_custom, null);

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

        popupView.findViewById(R.id.action_export_db).setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onExportLocal();
            }
        });

        popupView.findViewById(R.id.action_import_db).setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onImportLocal();
            }
        });

        popupView.findViewById(R.id.action_cloud_settings).setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onCloudSettings();
            }
        });

        popupView.findViewById(R.id.action_cloud_export).setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onCloudExport();
            }
        });

        popupView.findViewById(R.id.action_cloud_import).setOnClickListener(v -> {
            dismissMenu();
            if (listener != null) {
                listener.onCloudImport();
            }
        });

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int xOffset = dpToPx(context, 16);
        int yOffset = location[1] + dpToPx(context, 4);

        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.END, xOffset, yOffset);

        activePopupWindow = popupWindow;
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