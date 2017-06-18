package com.samsung.android.recoveryfile.modelfilewatcher;

/**
 * Created by samsung on 2017/5/5.
 */

public interface OnFileWatcherListener {
    public static final int FILE_CREATE = 0x001;
    public static final int FILE_DELETE = 0x002;
    void onFileChange(String path, int mode);
}