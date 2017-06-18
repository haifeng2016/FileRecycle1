package com.samsung.android.filerecycle.music;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by haifeng on 2017/6/14.
 */

public class ShowMusicFragment extends BaseFragment {

    protected MenuItem recycleMenuItem;
    protected MenuItem deleteMenuItem;

    private Set<FileBean> mSelectedDoc = new HashSet<>();
    protected View mMainView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_show_music, container, false);
        setHasOptionsMenu(true);
        title = mContext.getString(R.string.select_pic);
        updateActionBar();
        return mMainView;
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
                Toast.makeText(getActivity(),"已恢复 "+mSelectedDoc.size()+" 首歌曲",Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_delete:
                Toast.makeText(getActivity(),"已删除 "+mSelectedDoc.size()+" 首歌曲",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getActivity(),"请选择歌曲进行恢复或删除",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

}
