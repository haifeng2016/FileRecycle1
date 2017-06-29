package com.samsung.android.filerecycle.common;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

/**
 * Created by samsung on 2017/6/19.
 */

public interface IFileSelected {
    public void onFileSelected(FileBean fileBean, boolean checked);
}
