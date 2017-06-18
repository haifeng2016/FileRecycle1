package com.samsung.android.filerecycle.main;

import java.util.List;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.samsung.android.filerecycle.R;

/**
 * Created by samsung on 2017/5/9.
 */

public class GroupAdapter extends BaseAdapter {

    private List<FilesInfo> list;
    private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象
    protected LayoutInflater mInflater;
    private Context mContext;

    @Override
    public int getCount() {
        return list == null ? -1 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public GroupAdapter(Context context, List<FilesInfo> list) {
        this.list = list;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        FilesInfo filesInfo = list.get(position);
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_group_item, null);
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.group_image);
            viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.group_title);
            //viewHolder.mTextViewCounts = (TextView) convertView.findViewById(R.id.group_count);

            //用来监听ImageView的宽和高
            viewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {

                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                }
            });
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mImageView.setImageResource(R.drawable.cache);
        }

        viewHolder.mTextViewTitle.setText(filesInfo.getFolderName());
        //给ImageView设置路径Tag,这是异步加载图片的小技巧
        viewHolder.setType(filesInfo.getFileType());

        viewHolder.mImageView.setImageDrawable(mContext.getDrawable(filesInfo.getImgRes()));

        return convertView;
    }

    public static class ViewHolder{
        public MyImageView mImageView;
        public TextView mTextViewTitle;
        private int mFileType;
        public void setType(int type) {mFileType = type;}
        public int getType() {return mFileType;}
    }

    public static class FilesInfo {
        private int imgRes;
        private int fileType;
        private String folderName;

        public FilesInfo() {

        }

        public FilesInfo(String folder, int res, int type) {
            imgRes = res;
            fileType = type;
            folderName = folder;
        }

        public void setImgRes(@DrawableRes int imgRes) {
            this.imgRes = imgRes;
        }
        public int getImgRes() {
            return this.imgRes;
        }
        public void setFileType(int type) {
            this.fileType = type;
        }
        public int getFileType() {
            return this.fileType;
        }
        public void setFolderName(@NonNull String folderName) {
            this.folderName = folderName;
        }
        public String getFolderName() {
            return this.folderName;
        }
    }

}
