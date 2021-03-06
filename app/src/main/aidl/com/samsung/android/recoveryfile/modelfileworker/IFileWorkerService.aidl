// IFileWorkerService.aidl
package com.samsung.android.recoveryfile.modelfileworker;

import com.samsung.android.recoveryfile.modelfileworker.IFileWorkerListener;

// Declare any non-default types here with import statements
interface IFileWorkerService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void start();
    void stop();
    boolean available();
    int remainFdCnt();
    int openFile(String name);
    int closeFile(String name);
    int backupFile(String sname, String dname);
    String[] getOpenedFile();
    void setFileWorkerListener(IFileWorkerListener cb);
}
