package com.samsung.android.recoveryfile.modelfileworker;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.samsung.android.filerecycle.common.DLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by samsung on 2017/5/10.
 */

public class FileWorkerBinder extends IFileWorkerService.Stub {

    public static final int OPENFILE       = 0x001;
    public static final int BACKUPFILE     = 0x002;

    private HashMap<String, FileInputStream> fdHmap = new HashMap<String, FileInputStream>();
    private RemoteCallbackList<IFileWorkerListener> mCallbacks = new RemoteCallbackList<IFileWorkerListener>();
    private boolean mAvailable = true;
    private String mServiceName;

    public FileWorkerBinder(String serviceName){
        super();
        mServiceName = serviceName;
    }

    @Override
    public void start() throws RemoteException {

    }

    @Override
    public void stop() throws RemoteException {

    }

    @Override
    public int openFile(String name) throws RemoteException {
        int ret = 0;
        DLog.d("file " + name + " open in " + mServiceName);
        try {
            FileInputStream in = new FileInputStream(name);
            fdHmap.put(name, in);
            mAvailable = true;
        } catch (FileNotFoundException e) {
            DLog.d("file open failed " + e.getMessage());
            e.printStackTrace();
            mAvailable = false;
            ret = -1;
        } catch (Exception e) {
            DLog.d("open file error " + e.getMessage());
            mAvailable = true;
            ret = -1;
        }

        return ret;
    }

    @Override
    public int backupFile(String sname, String dname, int type) throws RemoteException {
        int ret = 0;
        FileInputStream in = null;
        FileOutputStream out = null;
        DLog.d("file " + sname + " backup in " + mServiceName);

        try {
            int hasRead;
            in = fdHmap.get(sname);
            File file = new File(dname);
            file.createNewFile();
            out = new FileOutputStream(file);
            byte[] buff = new byte[1024];

            while( (hasRead = in.read(buff)) > 0){
                out.write(buff, 0, hasRead);
                //Log.d(LOG_TAG, "read " + hasRead);
            }
            in.close();
            out.close();
            mAvailable = true;
            fdHmap.remove(sname);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ret = -1;
        }

        return ret;
    }

    @Override
    public boolean available() throws RemoteException {
        return mAvailable;
    }

    @Override
    public void setFileWorkerListener(IFileWorkerListener cb) throws RemoteException {
        mCallbacks.register(cb);
    }


}