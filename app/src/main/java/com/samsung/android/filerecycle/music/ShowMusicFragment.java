package com.samsung.android.filerecycle.music;

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

public class ShowMusicFragment extends BaseFragment implements IBasicAction {

    private List<FileBean> mFileList;
    private List<MusicListFileBean> mMusicList;
    private MainPresenter mPresenter;
    protected MenuItem recycleMenuItem;
    protected MenuItem deleteMenuItem;

    private Set<FileBean> mSelectedMusic = new HashSet<>();
    private ShowMusicActivity musicActivity = null;

    private MusicListAdapter musicListAdapter;
    protected View mMainView;

    private OnMainPresenterListener onMainPresenterListener = new OnMainPresenterListener() {
        @Override
        public void onReady(boolean ready) {
        }

        @Override
        public void onDeleteFiles(int success) {
            mSelectedMusic.clear();
            musicListAdapter.update();
            deleteMenuItem.setVisible(false);
            if (mFileList.size() <= 0) {
                musicListAdapter.updateCheckboxList();
            }
            updateOptionsMenu();
        }

        @Override
        public void onRecoverFiles(int success) {
            mSelectedMusic.clear();
            musicListAdapter.update();
            recycleMenuItem.setVisible(false);
            if (mFileList.size() <= 0) {
                musicListAdapter.updateCheckboxList();
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
            if (getActivity() instanceof ShowMusicActivity) {
                musicActivity = (ShowMusicActivity) getActivity();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_show_music, container, false);

        setHasOptionsMenu(true);
        title = mContext.getString(R.string.select_music);

        mPresenter = MainPresenter.getInstance(getActivity());
        mPresenter.setPresenterListener(onMainPresenterListener);
        initArgs();
        initMusicList();
        RecyclerView recyclerView = (RecyclerView) mMainView.findViewById(R.id.musicrecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(RecycleApplication.getApplication()));
        recyclerView.setHasFixedSize(true);
        musicListAdapter = new MusicListAdapter(this, mFileList);
        recyclerView.setAdapter(musicListAdapter);
        musicListAdapter.setOnItemClick(new MusicListAdapter.IItemAction() {
            @Override
            public void itemClick(int pos, String path) {
                Log.d("ShowMusicFragment", "itemClick" + pos + " " + path);
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
                        files.addAll(mSelectedMusic);
                        mPresenter.recoverFiles(files);
                    }
                });
                Toast.makeText(getActivity(),"已恢复 "+mSelectedMusic.size()+" 首歌曲",Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_delete:
                showDeleteMessage();
                break;
            default:
                Toast.makeText(getActivity(),"请选择歌曲进行恢复或删除",Toast.LENGTH_SHORT).show();
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
                                files.addAll(mSelectedMusic);
                                mPresenter.deleteFiles(files);
                            }
                        });
                        Toast.makeText(getActivity(), "已彻底删除 "+ mSelectedMusic.size()+" 首歌曲",Toast.LENGTH_SHORT).show();
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
            recycleMenuItem.setVisible(mSelectedMusic.size() > 0);
        }
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(mSelectedMusic.size() > 0);
        }
    }

    public void updateSelectedImagesList(FileBean fb, boolean selected) {
        if (mSelectedMusic == null) {
            mSelectedMusic = new HashSet<>();
        }
        if (selected) {
            mSelectedMusic.add(fb);
        } else {
            mSelectedMusic.remove(fb);
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

    private void initMusicList() {
        if (mMusicList == null) {
            mMusicList = new ArrayList<>();
        }
        mMusicList.clear();
        if (mFileList != null) {
            for (int i = 0; i < mFileList.size(); i++) {
                mMusicList.add(i, new MusicListFileBean(
                        mFileList.get(i).getName(),
                        mFileList.get(i).getDelTime(),
                        mFileList.get(i).getSize() + "",
                        mFileList.get(i).getSrcPath()));
            }
        }
        mMusicList.clear();
    }

    @Override
    public boolean onBackAction() {
        if (musicListAdapter.getCheckVisible()) {
            musicListAdapter.updateCheckboxList();
            if (mSelectedMusic != null) {
                mSelectedMusic.clear();
                updateOptionsMenu();
            }
            return true;
        }
        return false;
    }
}
