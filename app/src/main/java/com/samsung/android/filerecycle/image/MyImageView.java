package com.samsung.android.filerecycle.image;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

/**
 * Created by haifeng on 2017/5/9.
 * 一开始是继承ImageView，AS提示需要继承AppCompatImageView，同时，增加了MyImageView(Context context)构造函数
 */

public class MyImageView extends AppCompatImageView {

    private OnMeasureListener onMeasureListener;

    public void setOnMeasureListener(OnMeasureListener onMeasureListener) {
        this.onMeasureListener = onMeasureListener;
    }

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //将图片测量的大小回调到onMeasureSize()方法中
        if(onMeasureListener != null){
            onMeasureListener.onMeasureSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public interface OnMeasureListener{
        public void onMeasureSize(int width, int height);
    }

}
