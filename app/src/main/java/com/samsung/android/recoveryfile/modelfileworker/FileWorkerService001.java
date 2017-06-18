package com.samsung.android.recoveryfile.modelfileworker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FileWorkerService001 extends Service {
    private FileWorkerBinder mFileWorkerBinder;

    public FileWorkerService001() {
        mFileWorkerBinder = new FileWorkerBinder(getClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mFileWorkerBinder;
    }
}
