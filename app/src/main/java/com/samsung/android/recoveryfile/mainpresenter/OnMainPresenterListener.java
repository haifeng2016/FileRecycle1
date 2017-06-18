package com.samsung.android.recoveryfile.mainpresenter;

/**
 * Created by samsung on 2017/5/17.
 */

public interface OnMainPresenterListener {
    void onReady(boolean ready);
    void onDeleteFiles(int success);
    void onRecoverFiles(int success);
    void onBackupFiles(FileBean m);
}
