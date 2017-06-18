package com.samsung.android.recoveryfile.modelfilethumbnail;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

/**
 * Created by samsung on 2017/6/16.
 */

public interface IFileThumbnail {
    void start();
    void stop();
    void setFileThumbnailListener(OnFileThumbnailListener m);
    String getSysThumbnail(FileBean fb);
    int createThumbnail(FileBean fb);
}
