package com.samsung.android.recoveryfile.modelfileworker;

import android.content.Context;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

/**
 * Created by samsung on 2017/5/17.
 */

public interface IFileWorker {
    public void start(Context context, OnFileWorkerListener m);
    public void stop();
    public int openFile(String name);
    public int closeFile(String name);
    public boolean isReady();
    public int backupFile(FileBean fb);
    public int recoverFile(FileBean fb);
}
