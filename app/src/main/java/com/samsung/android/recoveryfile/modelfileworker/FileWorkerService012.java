package com.samsung.android.recoveryfile.modelfileworker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by samsung on 2017/6/12.
 */

public class FileWorkerService012 extends Service {
    private FileWorkerBinder mFileWorkerBinder;

    public FileWorkerService012() {
        mFileWorkerBinder = new FileWorkerBinder(getClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mFileWorkerBinder;
    }
}