package com.samsung.android.filerecycle.video;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;
import com.samsung.android.filerecycle.common.IBasicAction;
import com.samsung.android.filerecycle.common.RecycleApplication;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.mainpresenter.MainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.OnMainPresenterListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by haifeng on 2017/6/14.
 */

public class ShowVideoFragment extends BaseFragment implements IBasicAction {

    private List<FileBean> mFileList;
    private List<VideoListFileBean> mVideoList;
    private MainPresenter mPresenter;
    protected MenuItem recycleMenuItem;
    protected MenuItem deleteMenuItem;

    private Set<FileBean> mSelectedVideo = new HashSet<>();
    private ShowVideoActivity videoActivity = null;

    private VideoListAdapter videoListAdapter;
    protected View mMainView;

    private OnMainPresenterListener onMainPresenterListener = new OnMainPresenterListener() {
        @Override
        public void onReady(boolean ready) {
        }

        @Override
        public void onDeleteFiles(int success) {
            mSelectedVideo.clear();
            videoListAdapter.update();
            deleteMenuItem.setVisible(false);
            if (mFileList.size() <= 0) {
                videoListAdapter.updateCheckboxList();
            }
            updateOptionsMenu();
        }

        @Override
        public void onRecoverFiles(int success) {
            mSelectedVideo.clear();
            videoListAdapter.update();
            recycleMenuItem.setVisible(false);
            if (mFileList.size() <= 0) {
                videoListAdapter.updateCheckboxList();
            }
            updateOptionsMenu();
        }

        @Override
        public void onBackupFiles(FileBean m) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (getActivity() instanceof ShowVideoActivity) {
                videoActivity = (ShowVideoActivity) getActivity();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_show_video, container, false);

        setHasOptionsMenu(true);
        title = mContext.getString(R.string.select_video);

        mPresenter = MainPresenter.getInstance(getActivity());
        mPresenter.setPresenterListener(onMainPresenterListener);
        initArgs();
        initVideoList();
        RecyclerView recyclerView = (RecyclerView) mMainView.findViewById(R.id.videorecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(RecycleApplication.getApplication()));
        recyclerView.setHasFixedSize(true);
        videoListAdapter = new VideoListAdapter(this, mFileList);
        recyclerView.setAdapter(videoListAdapter);
        videoListAdapter.setOnItemClick(new VideoListAdapter.IItemAction() {
            @Override
            public void itemClick(int pos, String path) {
                Log.d("ShowVideoFragment", "itemClick" + pos + " " + path);
            }
        });
        updateActionBar();
        return mMainView;
    }

    @Override
    public void onFileSelected(FileBean fileBean, boolean checked) {
        updateSelectedImagesList(fileBean, checked);
        updateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.show_image_menu, menu);
        recycleMenuItem = menu.findItem(R.id.action_recycle);
        deleteMenuItem = menu.findItem(R.id.action_delete);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recycle:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<FileBean> files = new ArrayList<>();
                        files.addAll(mSelectedVideo);
                        mPresenter.recoverFiles(files);
                    }
               });
                Toast.makeText(getActivity(),"已恢复 "+mSelectedVideo.size()+" 个视频",Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_delete:
                showDeleteMessage();
                break;
            default:
                Toast.makeText(getActivity(),"请选择视频进行恢复或删除",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //当用户点击了“删除”按键后，弹框提示是否真需要删除，避免误操作
    private void showDeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setMessage("确定要删除这些文件吗？点击【确定】后文件将被彻底删除！");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                List<FileBean> files = new ArrayList<>();
                                files.addAll(mSelectedVideo);
                                mPresenter.deleteFiles(files);
                            }
                        });
                        Toast.makeText(getActivity(), "已彻底删除 "+ mSelectedVideo.size()+" 个视频",Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        builder.show();
    }

    protected void updateOptionsMenu() {
        if (recycleMenuItem != null) {
            recycleMenuItem.setVisible(mSelectedVideo.size() > 0);
        }
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(mSelectedVideo.size() > 0);
        }
    }

    public void updateSelectedImagesList(FileBean fb, boolean selected) {
        if (mSelectedVideo == null) {
            mSelectedVideo = new HashSet<>();
        }
        if (selected) {
            mSelectedVideo.add(fb);
        } else {
            mSelectedVideo.remove(fb);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initArgs() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            int type = arguments.getInt("type");
            if(mFileList == null) {
                mFileList = new ArrayList<>();
            }
            mFileList.clear();
            mFileList = mPresenter.getBackupFiles(type);
        }
    }

    private void initVideoList() {
        if (mVideoList == null) {
            mVideoList = new ArrayList<>();
        }
        mVideoList.clear();
        if (mFileList != null) {
            for (int i = 0; i < mFileList.size(); i++) {
                mVideoList.add(i, new VideoListFileBean(
                        mFileList.get(i).getName(),
                        mFileList.get(i).getDelTime(),
                        mFileList.get(i).getSize() + "",
                        mFileList.get(i).getSrcPath()));
            }
        }
        mVideoList.clear();
    }

    @Override
    public boolean onBackAction() {
        if (videoListAdapter.getCheckVisible()) {
            videoListAdapter.updateCheckboxList();
            if (mSelectedVideo != null) {
                mSelectedVideo.clear();
                updateOptionsMenu();
            }
            return true;
        }
        return false;
    }
}
