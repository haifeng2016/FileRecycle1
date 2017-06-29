package com.samsung.android.recoveryfile.mainpresenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.samsung.android.filerecycle.common.DLog;

import java.util.List;

/**
 * Created by samsung on 2017/5/5.
 */

public class MainPresenter implements IMainPresenter {
    private static MainPresenter instance = null;
    private IMainPresenter mIMainPresenter = null;
    private OnMainPresenterListener mMainPresenterListner = null;

    public synchronized static MainPresenter getInstance(Context context){
        if(instance == null){
            instance = new MainPresenter(context);
        }
        return instance;
    }

    public MainPresenter(Context context) {
        Intent bindeMainPresenter = new Intent(context, MainPresenterService.class);
        context.bindService(bindeMainPresenter, mainPresenterServiceConn, Context.BIND_AUTO_CREATE);
        DLog.d("MainPresenter service bind trigger.");
    }

    private ServiceConnection mainPresenterServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIMainPresenter = ((MainPresenterService.MainPresenterBinder)service).getService();
            mIMainPresenter.setPresenterListener(mMainPresenterListner);
            DLog.d("MainPresenter service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIMainPresenter = null;
            DLog.d("MainPresenter service disconnected");
        }
    };

    @Override
    public int start() {
        return 0;
    }

    @Override
    public int stop() {
        if(mIMainPresenter != null)
            return mIMainPresenter.stop();

        return 0;
    }

    @Override
    public boolean isReady() {
        if(mIMainPresenter != null)
            return mIMainPresenter.isReady();

        return false;
    }

    @Override
    public List<FileBean> getBackupFiles(int type) {
        if(mIMainPresenter != null)
            return mIMainPresenter.getBackupFiles(type);

        return null;
    }

    @Override
    public int recoverFiles(List<FileBean> list) {
        if(mIMainPresenter != null)
            return mIMainPresenter.recoverFiles(list);

        return 0;
    }

    @Override
    public int deleteFiles(List<FileBean> list) {
        if(mIMainPresenter != null)
            return mIMainPresenter.deleteFiles(list);

        return 0;
    }

    @Override
    public int setFileWatcherType(int[] type, boolean[] set) {
        if(mIMainPresenter != null)
            return mIMainPresenter.setFileWatcherType(type, set);

        return 0;
    }

    @Override
    public int setAutoDeleteTime(long time) {
        if(mIMainPresenter != null)
            return mIMainPresenter.setAutoDeleteTime(time);

        return 0;
    }

    @Override
    public int setPresenterListener(OnMainPresenterListener listener) {
        mMainPresenterListner = listener;
        if(mIMainPresenter != null)
            return mIMainPresenter.setPresenterListener(mMainPresenterListner);

        return 0;
    }
}
