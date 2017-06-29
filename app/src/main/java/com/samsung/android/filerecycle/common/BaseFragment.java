package com.samsung.android.filerecycle.common;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;

public abstract class BaseFragment extends Fragment implements IFileSelected {

    protected Context mContext = null;
    protected String title;
    protected View customActionBarView;
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onFileSelected(FileBean fileBean, boolean checked) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) {
            return;
        }

        mContext = getActivity().getApplicationContext();
    }

    public void updateActionBar() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        int displayOptions = actionBar.getDisplayOptions();

        if ((displayOptions & ActionBar.DISPLAY_SHOW_TITLE) > 0) {
            if (title != null) {
                actionBar.setTitle(title);
            } else {
                actionBar.setTitle(null);
            }
        }

        if ((displayOptions & ActionBar.DISPLAY_SHOW_CUSTOM) > 0) {
            if (customActionBarView != null) {
                actionBar.setCustomView(customActionBarView);
            }

            View v = actionBar.getCustomView();
            if (v !=null) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                v.setLayoutParams(lp);
            }

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeCallbacksAndMessages(null);
    }

}
