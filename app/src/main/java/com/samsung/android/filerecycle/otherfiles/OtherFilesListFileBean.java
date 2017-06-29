package com.samsung.android.filerecycle.otherfiles;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by haifeng on 2017/6/21.
 */

public class OtherFilesListFileBean {
    private String name;
    private String time;
    private String size;
    private String path;
    private Date delTime;
    private boolean selected = false;

    public OtherFilesListFileBean(@NonNull String name, @NonNull String time,
                                  @NonNull String size, @NonNull String path) {
        this.name = name;
        this.time = time;
        this.size = size;
        this.path = path;
    }

    public OtherFilesListFileBean(@NonNull String name, @NonNull Date delTime,
                                  @NonNull String size, @NonNull String path) {
        this.name = name;
        this.delTime = delTime;
        this.size = size;
        this.path = path;
        this.time = delTime.toString();
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public Date getDelTime() {
        return delTime;
    }

    public String getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public void setSelected(boolean check) {
        this.selected = check;
    }

    public boolean getSelected() {
        return selected;
    }
}
