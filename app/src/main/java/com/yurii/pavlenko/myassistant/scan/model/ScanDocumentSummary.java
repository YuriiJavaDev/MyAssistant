package com.yurii.pavlenko.myassistant.scan.model;

import androidx.room.Embedded;

/** A document as shown in the list: its first page serves as the cover. */
public class ScanDocumentSummary {

    @Embedded
    public ScanDocument document;

    public int pageCount;

    public String coverPath;
}
