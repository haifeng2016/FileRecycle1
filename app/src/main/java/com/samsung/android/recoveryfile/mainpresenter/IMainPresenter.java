package com.samsung.android.recoveryfile.mainpresenter;

import java.util.List;

/**
 * Created by samsung on 2017/5/17.
 */

public interface IMainPresenter {
    int start();
    int stop();
    boolean isReady();
    List<FileBean> getBackupFiles(int type);
    int recoverFiles(List<FileBean> list);
    int deleteFiles(List<FileBean> list);
    int setFileWatcherType(String type[]);
    int setAutoDeleteTime(int time);
    int setPresenterListener(OnMainPresenterListener listener);
}
