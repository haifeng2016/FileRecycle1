package com.samsung.android.recoveryfile.modelfilewatcher;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

/**
 * Created by samsung on 2017/5/5.
 */

public interface OnFileWatcherListener {
    public static final int FILE_CREATE = 0x001;
    public static final int FILE_DELETE = 0x002;
    public static final int FILE_CLOSE  = 0x003;
    int onFileChange(String path, int mode, FileBean fb);
}