package com.yurii.pavlenko.myassistant.backup;

import androidx.annotation.Nullable;

/** Why a cloud action failed, without any wording: the screen words it in the language of the user. */
public final class BackupFailure {

    public enum Kind {
        INSECURE_URL,
        AUTHENTICATION,
        SERVER_CODE,
        CONNECTION,
        UPLOAD,
        DOWNLOAD
    }

    private final Kind kind;
    private final int code;
    private final String detail;

    private BackupFailure(Kind kind, int code, @Nullable String detail) {
        this.kind = kind;
        this.code = code;
        this.detail = detail;
    }

    public static BackupFailure of(Kind kind) {
        return new BackupFailure(kind, 0, null);
    }

    public static BackupFailure serverCode(int code) {
        return new BackupFailure(Kind.SERVER_CODE, code, null);
    }

    /** A failure with the technical reason reported by the system, which may be missing. */
    public static BackupFailure withDetail(Kind kind, @Nullable String detail) {
        return new BackupFailure(kind, 0, detail);
    }

    public Kind getKind() {
        return kind;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public String getDetail() {
        return detail;
    }
}
