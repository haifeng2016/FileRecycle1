// IFileWorkerListener.aidl
package com.samsung.android.recoveryfile.modelfileworker;

// Declare any non-default types here with import statements

interface IFileWorkerListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onFileOpened(String name);
    void onFileBackuped(String name);
}
