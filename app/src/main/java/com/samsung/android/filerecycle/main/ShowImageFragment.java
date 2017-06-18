package com.samsung.android.filerecycle.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.mainpresenter.MainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.OnMainPresenterListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by haifeng on 2017/5/12.
 */

public class ShowImageFragment extends BaseFragment {

    private GridView mGridView;
    private List<FileBean> mFileList;
    private ChildAdapter adapter;

    protected View view;
    protected CheckBox allCheckBox;
    protected LinearLayout allSelectLinearLayout;
    protected ImageView allSelectDividerImageView;
    protected MenuItem recycleMenuItem;
    protected MenuItem deleteMenuItem;

    private Set<FileBean> mSelectedImages = new HashSet<>();

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    MainPresenter mPresenter;

    private ShowImageActivity activity = null;

    private OnMainPresenterListener onMainPresenterListener = new OnMainPresenterListener() {
        @Override
        public void onReady(boolean ready) {
        }

        @Override
        public void onDeleteFiles(int success) {
            mSelectedImages.clear();
            adapter.update();
            Log.e("RecycleDebug", "onDeleteFile");
            deleteMenuItem.setVisible(false);
            updateAllCheckBoxState();
            updateOptionsMenu();
        }

        @Override
        public void onRecoverFiles(int success) {
            mSelectedImages.clear();
            adapter.update();
            Log.e("RecycleDebug", "onRecoverFile");
            recycleMenuItem.setVisible(false);
            updateAllCheckBoxState();
            updateOptionsMenu();
        }

        @Override
        public void onBackupFiles(FileBean m) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (getActivity() instanceof ShowImageActivity) {
                activity = (ShowImageActivity) getActivity();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_image, container, false);

        setHasOptionsMenu(true);
        title = mContext.getString(R.string.select_pic);

        mPresenter = MainPresenter.getInstance(getActivity());
        mPresenter.setPresenterListener(onMainPresenterListener);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mGridView = (GridView) view.findViewById(R.id.child_grid);
            int type = arguments.getInt("type");
            mFileList = mPresenter.getBackupFiles(type);
            if(mFileList == null) {
                mFileList = new ArrayList<>();
            }
            adapter = new ChildAdapter(getActivity(), mFileList, mGridView, this);
            mGridView.setAdapter(adapter);
        }
        updateActionBar();

        allSelectLinearLayout = (LinearLayout) view.findViewById(R.id.all_checkbox_layout);
        allSelectLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allCheckBox.setChecked(!allCheckBox.isChecked());
            }
        });
        allCheckBox = (CheckBox) view.findViewById(R.id.all_checkbox);
        allCheckBox.setOnCheckedChangeListener(allBtnCheckedListener);
        allSelectDividerImageView = (ImageView) view.findViewById(R.id.allselectdivider);

        updateAllCheckBoxState();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllCheckBoxState();
    }

    //如果一进去没有图片，则不显示全选的checkbox
    private void updateAllCheckBoxState() {
        if (mFileList == null) {
            return;
        }
        if (mFileList.size() > 0) {
            allSelectLinearLayout.setVisibility(View.VISIBLE);
            allSelectDividerImageView.setVisibility(View.VISIBLE);
        } else {
            allSelectLinearLayout.setVisibility(View.GONE);
            allSelectDividerImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.show_image_menu,menu);

        recycleMenuItem = menu.findItem(R.id.action_recycle);
        deleteMenuItem = menu.findItem(R.id.action_delete);

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recycle:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<FileBean> files = new ArrayList<>();
                        files.addAll(mSelectedImages);
                        mPresenter.recoverFiles(files);
                    }
                });
                Toast.makeText(getActivity(), "已恢复 "+mSelectedImages.size()+" 张图片",Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_delete:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<FileBean> files = new ArrayList<>();
                        files.addAll(mSelectedImages);
                        mPresenter.deleteFiles(files);
                    }
                });
                Toast.makeText(getActivity(), "已删除 "+ mSelectedImages.size()+" 张图片",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getActivity(), "请选择图片进行恢复或删除",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    protected void updateOptionsMenu() {
        if (recycleMenuItem != null ) {
            recycleMenuItem.setVisible(mSelectedImages.size() > 0);
        }
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(mSelectedImages.size() > 0);
        }
    }

    public void updateSelectedImagesList(FileBean fb, boolean selected) {
        if (mSelectedImages == null) {
            mSelectedImages = new HashSet<>();
        }
        if (selected) {
            mSelectedImages.add(fb);
        } else {
            mSelectedImages.remove(fb);
        }
    }

    CompoundButton.OnCheckedChangeListener allBtnCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Iterator<FileBean> it = mFileList.iterator();
                while (it.hasNext()) {
                    FileBean t = it.next();
                    mSelectedImages.add(t);
                }
            } else {
                mSelectedImages.clear();
            }
            adapter.updateAllSelectedItems(isChecked);
            adapter.notifyDataSetChanged();
            updateOptionsMenu();
        }
    };

    public void updateSelectedCheckBox() {
        allCheckBox.setOnCheckedChangeListener(null);
        allCheckBox.setChecked(mSelectedImages.size() > 0 && mSelectedImages.size() == mFileList.size());
        allCheckBox.setOnCheckedChangeListener(allBtnCheckedListener);
    }

}

class ChildAdapter extends BaseAdapter {

    private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象
    /**
     * 用来存储图片的选中情况
     */
    private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
    private GridView mGridView;
    private List<FileBean> mList;
    private ShowImageFragment mFragment;
    protected LayoutInflater mInflater;

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ChildAdapter(@NonNull Context context, @NonNull List<FileBean> list,
                        @NonNull GridView mGridView, @NonNull ShowImageFragment fragment) {
        this.mList = list;
        this.mGridView = mGridView;
        mInflater = LayoutInflater.from(context);
        this.mFragment = fragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        String path;
        if(mList.get(position).getType()==FileBean.TYPE.IMG)
            path = mList.get(position).getThumbnailPath();
        else
            path = mList.get(position).getBackupPath();

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_child_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.child_image);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.child_checkbox);

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
        viewHolder.mImageView.setTag(path);
        viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = viewHolder.mCheckBox;
                checkBox.setChecked(!checkBox.isChecked());
                updateSelectedItems(position, viewHolder.mCheckBox.isChecked(), viewHolder.mCheckBox);
            }
        });
        viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSelectedItems(position, isChecked, viewHolder.mCheckBox);
            }
        });

        viewHolder.mCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);

        //利用NativeImageLoader类加载本地图片
        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint, new NativeImageLoader.NativeImageCallBack() {

            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
                if(bitmap != null && mImageView != null){
                    mImageView.setImageBitmap(bitmap);
                }
            }
        });

        if (bitmap != null) {
            viewHolder.mImageView.setImageBitmap(bitmap);
        }else{
            viewHolder.mImageView.setImageResource(R.drawable.cache);
        }

        return convertView;
    }

    private void updateSelectedItems(int position, boolean checked, View checkedView) {
        if(!mSelectMap.containsKey(position) || !mSelectMap.get(position)){
            addAnimation(checkedView);
        }
        mSelectMap.put(position, checked);
        mFragment.updateSelectedImagesList(mList.get(position), checked);
        mFragment.updateSelectedCheckBox();
        mFragment.updateOptionsMenu();
    }

    public void updateAllSelectedItems(boolean checked) {
        for (int i = 0; i < mList.size(); i++) {
            mSelectMap.put(i, checked);
        }
    }

    public void unCheckAll() {
        if (mSelectMap == null) {
            return;
        }
        for (int i = 0; i < mSelectMap.size(); i++) {
            mSelectMap.put(i, false);
        }
    }

    public void update() {
        unCheckAll();
        notifyDataSetChanged();
    }

    /**
     * 给CheckBox加点击动画，利用开源库nineoldandroids设置动画
     * @param view
     */
    private void addAnimation(View view){
        float [] values = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", values),
                ObjectAnimator.ofFloat(view, "scaleY", values));
        set.setDuration(150);
        set.start();
    }

    public static class ViewHolder{
        public MyImageView mImageView;
        public CheckBox mCheckBox;
    }

}
