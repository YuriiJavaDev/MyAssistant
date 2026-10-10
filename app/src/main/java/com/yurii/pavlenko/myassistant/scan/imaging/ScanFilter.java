package com.yurii.pavlenko.myassistant.scan.imaging;

/** How colour is treated in the finished scan. */
public enum ScanFilter {
    COLOR("Color"),
    GRAYSCALE("Gray"),
    BLACK_WHITE("B&W");

    private final String label;

    ScanFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
