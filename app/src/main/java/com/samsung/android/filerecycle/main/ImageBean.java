package com.samsung.android.filerecycle.main;

/**
 * Created by haifeng on 2017/5/9.
 */

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * GridView的每个item的数据对象
 */
public class ImageBean {
    /**
     * 文件夹的第一张图片路径
     */
    private String topImagePath;
    /**
     * 文件夹名
     */
    private String folderName;
    /**
     * 文件夹中的图片数
     */
    private int imageCounts;

    private int imageRes;

    public String getTopImagePath() {
        return topImagePath;
    }
    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }
    public int getImageRes() {return imageRes;}
    public void setImageRes(@DrawableRes int res) {
        this.imageRes = res;
    }
    private int type;
    public void setType(int type) {
        this.type = type;
    }
    public int getType(){
        return type;
    }
    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(@NonNull String folderName) {
        this.folderName = folderName;
    }
    public int getImageCounts() {
        return imageCounts;
    }
    public void setImageCounts(int imageCounts) {
        this.imageCounts = imageCounts;
    }

}
