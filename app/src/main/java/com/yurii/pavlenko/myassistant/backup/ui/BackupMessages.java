package com.yurii.pavlenko.myassistant.backup.ui;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.backup.BackupFailure;
import com.yurii.pavlenko.myassistant.backup.SyncResult;

import java.util.ArrayList;
import java.util.List;

/** Words the outcome of a cloud action for the user. */
final class BackupMessages {

    private BackupMessages() {
    }

    @NonNull
    static String success(Context context, SyncResult result) {
        switch (result.getKind()) {
            case UPLOADED:
                return context.getString(R.string.backup_cloud_upload_success);
            case RESTORED:
                return context.getString(R.string.backup_cloud_restore_success);
            default:
                return context.getString(R.string.backup_cloud_restore_partly, missingParts(context, result));
        }
    }

    @NonNull
    static String failure(Context context, BackupFailure failure) {
        switch (failure.getKind()) {
            case INSECURE_URL:
                return context.getString(R.string.backup_error_insecure_url);
            case AUTHENTICATION:
                return context.getString(R.string.backup_error_authentication);
            case SERVER_CODE:
                return context.getString(R.string.backup_error_server_code, failure.getCode());
            case CONNECTION:
                return context.getString(R.string.backup_error_connection, detail(context, failure));
            case UPLOAD:
                return context.getString(R.string.backup_error_upload, detail(context, failure));
            default:
                return context.getString(R.string.backup_error_download, detail(context, failure));
        }
    }

    private static String detail(Context context, BackupFailure failure) {
        return failure.getDetail() != null ? failure.getDetail() : context.getString(R.string.backup_error_unknown);
    }

    private static String missingParts(Context context, SyncResult result) {
        List<String> names = new ArrayList<>();
        for (SyncResult.Part part : result.getMissing()) {
            switch (part) {
                case STEPS:
                    names.add(context.getString(R.string.backup_part_steps));
                    break;
                case FLIGHTS:
                    names.add(context.getString(R.string.backup_part_flights));
                    break;
                default:
                    names.add(context.getString(R.string.backup_part_scans));
                    break;
            }
        }
        return TextUtils.join(context.getString(R.string.backup_part_separator), names);
    }
}
