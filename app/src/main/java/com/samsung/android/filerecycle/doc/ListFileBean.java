package com.samsung.android.filerecycle.doc;

import android.support.annotation.NonNull;

/**
 * Created by haifeng on 2017/6/14.
 */

public class ListFileBean {
    private String name;
    private String time;
    private String size;
    private String path;
    private boolean selected = false;

    public ListFileBean(@NonNull String name, @NonNull String time,
                        @NonNull String size, @NonNull String path) {
        this.name = name;
        this.time = time;
        this.size = size;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
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
