package com.samsung.android.recoveryfile.modelfilewatcher;

/**
 * Created by samsung on 2017/5/17.
 */

public interface IFileWatcher {
    public void setWatcherPath(String path);
    public void clearWatcherPath(String path);
    public void setFileListener(OnFileWatcherListener m);
    public void start();
    public void stop();
}
