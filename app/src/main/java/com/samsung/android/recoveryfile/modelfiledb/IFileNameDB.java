package com.samsung.android.recoveryfile.modelfiledb;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.util.List;

/**
 * Created by samsung on 2017/5/17.
 */

public interface IFileNameDB {
    public void start();
    public void stop();
    public int saveFileName(FileBean node);
    public int delteFileName(FileBean node);
    public List<FileBean> getFileName(int type);
}
