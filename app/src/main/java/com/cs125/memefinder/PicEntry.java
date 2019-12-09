package com.cs125.memefinder;

import android.net.Uri;

public class PicEntry {
    public String fileName;
    public String fileText;
    public transient Uri fileUri;
    public PicEntry(String toFileName, String toFileText, Uri toFileUri) {
        this.fileName = toFileName;
        this.fileText = toFileText;
        this.fileUri = toFileUri;
    }
}
