package com.samsung.android.recoveryfile.modelfileworker;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

/**
 * Created by samsung on 2017/6/9.
 */

public interface OnFileWorkerListener {
    void onReady();
    void onFileOpened(String name, int retval);
    void onFileBackuped(FileBean fb, int retval);
    void onFileRecovered(FileBean fb, int retval);
}
