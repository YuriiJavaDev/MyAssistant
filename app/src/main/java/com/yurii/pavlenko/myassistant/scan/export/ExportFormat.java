package com.yurii.pavlenko.myassistant.scan.export;

/** File types a scan can be exported as. */
public enum ExportFormat {
    PDF("application/pdf", ".pdf"),
    JPEG("image/jpeg", ".jpg");

    private final String mimeType;
    private final String extension;

    ExportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
