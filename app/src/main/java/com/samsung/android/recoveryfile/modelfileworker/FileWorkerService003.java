package com.samsung.android.recoveryfile.modelfileworker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.samsung.android.filerecycle.common.DLog;

public class FileWorkerService003 extends Service {
    private FileWorkerBinder mFileWorkerBinder = null;

    public FileWorkerService003() {
        mFileWorkerBinder = new FileWorkerBinder(getClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mFileWorkerBinder;
    }

    @Override
    public void onCreate() {
        DLog.d("filewservice onCreate");
        super.onCreate();
        if(mFileWorkerBinder != null){
            mFileWorkerBinder.onCreate();
        }
    }

    @Override
    public void onDestroy() {
        DLog.d("filewservice onDestroy");
        super.onDestroy();
        if(mFileWorkerBinder != null){
            mFileWorkerBinder.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        DLog.d("filewservice onLowMemory");
        super.onLowMemory();
        if(mFileWorkerBinder != null){
            mFileWorkerBinder.onLowMemory();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        DLog.d("filewservice onUnbind");
        if(mFileWorkerBinder != null){
            mFileWorkerBinder.onUnbind();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        DLog.d("filewservice onRebind");
        super.onRebind(intent);
        if(mFileWorkerBinder != null){
            mFileWorkerBinder.onRebind();
        }
    }
}
