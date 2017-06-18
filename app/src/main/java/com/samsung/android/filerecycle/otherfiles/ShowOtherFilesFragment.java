package com.samsung.android.filerecycle.otherfiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;

/**
 * Created by haifeng on 2017/6/17.
 */

public class ShowOtherFilesFragment extends BaseFragment {

    private ViewGroup view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_other_files,container,false);
        title = mContext.getString(R.string.select_pic);
        updateActionBar();
        return view;
    }

}
