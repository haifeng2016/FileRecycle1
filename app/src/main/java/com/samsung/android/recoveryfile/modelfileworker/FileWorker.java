package com.samsung.android.recoveryfile.modelfileworker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by samsung on 2017/5/9.
 */

public class FileWorker implements IFileWorker {
    private final static int WORKER_SERVICE_NUM = 4;
    private IFileWorkerService []mFileWorkerServiceArray = new IFileWorkerService[WORKER_SERVICE_NUM];
    private IFileWorkerService mCachedWorkerService = null;
    private HashMap<String, IFileWorkerService> mFileMap = new HashMap<String, IFileWorkerService>();
    private HashMap<String, FileBean> mBackupMap = new HashMap<>();
    private HashMap<String, FileBean> mRecoverMap = new HashMap<>();
    private OnFileWorkerListener mOnFileWorkerListener = null;
    private Context mContext;
    private FileWorker.Token token = null;
    private Handler mHandler;
    private Handler mMainHandler;
    private HandlerThread mHanderThread;
    private boolean mReady = false;
    public static final int OPENFILE         = 0x001;
    public static final int BACKUPFILE       = 0x002;
    public static final int RECOVERFILE      = 0x003;
    public static final int WORKREADY        = 0x004;

    @Override
    public void start(Context context, OnFileWorkerListener m) {
        for(int i=0; i<WORKER_SERVICE_NUM; i++){
            mFileWorkerServiceArray[i] = null;
        }

        this.mContext = context;
        this.mOnFileWorkerListener = m;
        token = new FileWorker.Token();

        DLog.d("start services in file worker");
//        Intent intent = new Intent(mContext, FileWorkerService000.class);
//        mContext.bindService(intent, workerserviceConn, Context.BIND_AUTO_CREATE);

        for(int n = 0; n < WORKER_SERVICE_NUM; n++) {
            Intent intent = null;
            switch(n){
                case 0:
                    intent = new Intent(mContext, FileWorkerService000.class);
                    break;
                case 1:
                    intent = new Intent(mContext, FileWorkerService001.class);
                    break;
                case 2:
                    intent = new Intent(mContext, FileWorkerService002.class);
                    break;
                case 3:
                    intent = new Intent(mContext, FileWorkerService003.class);
                    break;
                case 4:
                    intent = new Intent(mContext, FileWorkerService004.class);
                    break;
                case 5:
                    intent = new Intent(mContext, FileWorkerService005.class);
                    break;
                case 6:
                    intent = new Intent(mContext, FileWorkerService006.class);
                    break;
                case 7:
                    intent = new Intent(mContext, FileWorkerService007.class);
                    break;
                case 8:
                    intent = new Intent(mContext, FileWorkerService008.class);
                    break;
                case 9:
                    intent = new Intent(mContext, FileWorkerService009.class);
                    break;
                case 10:
                    intent = new Intent(mContext, FileWorkerService010.class);
                    break;
                case 11:
                    intent = new Intent(mContext, FileWorkerService011.class);
                    break;
                case 12:
                    intent = new Intent(mContext, FileWorkerService012.class);
                    break;
                case 13:
                    intent = new Intent(mContext, FileWorkerService013.class);
                    break;
                case 14:
                    intent = new Intent(mContext, FileWorkerService014.class);
                    break;
                case 15:
                    intent = new Intent(mContext, FileWorkerService015.class);
                    break;
                default:
                    break;
            }
            if(intent != null)
                mContext.bindService(intent, workerserviceConn, Context.BIND_AUTO_CREATE);
        }

        mHanderThread = new HandlerThread("worker_thread");
        mHanderThread.start();
        mHandler = new workerHandler(mHanderThread.getLooper());
        mMainHandler = new MyMainThreadHandler(Looper.getMainLooper());
    }

    @Override
    public void stop() {

    }

    private class workerHandler extends Handler {
        public workerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int ret = 0;
            String sname = msg.getData().getString("sname");
            String dname = msg.getData().getString("dname");
            int type = msg.arg1;
            Message sent = mMainHandler.obtainMessage();
            sent.what = msg.what;

            switch(msg.what){
                case OPENFILE:
                    String name = (String)msg.obj;
                    ret = openFileLocal(name);
                    sent.obj = name;
                    break;
                case BACKUPFILE:
                    ret = backupFileLocal(sname, dname, type);
                    sent.obj = sname;
                    sent.arg1 = ret;
                    break;
                case RECOVERFILE:
                    ret = recoverFileLocal(sname, dname, type);
                    sent.obj = sname;
                    break;
                default:
                    break;
            }
            sent.arg1 = ret;
            mMainHandler.sendMessage(sent);
        }
    }

    class MyMainThreadHandler extends Handler {
        public MyMainThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FileBean fb;
            int ret = msg.arg1;
            String name = (String)msg.obj;
            switch(msg.what) {
                case OPENFILE:
                    mOnFileWorkerListener.onFileOpened(name, ret);
                    break;
                case BACKUPFILE:
                    fb = mBackupMap.get(name);
                    mOnFileWorkerListener.onFileBackuped(fb, ret);
                    break;
                case RECOVERFILE:
                    fb = mRecoverMap.get(name);
                    mOnFileWorkerListener.onFileRecovered(fb, ret);
                    break;
                case WORKREADY:
                    mOnFileWorkerListener.onReady();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int openFile(String name) {
        Message msg = mHandler.obtainMessage();
        msg.what = OPENFILE;
        msg.obj = name;
        mHandler.sendMessage(msg);
        return 0;
    }

    private int openFileLocal(String name) {
        boolean keep = true;
        DLog.d("open file local 1");
        if(mCachedWorkerService != null){
            try {
                if(mCachedWorkerService.openFile(name) >= 0){
                    mFileMap.put(name, mCachedWorkerService);
                    //DLog.d(name + "open pass");
                    return 0;
                }else if(mCachedWorkerService.available()){
                    DLog.d(name + "open fail but avaiable");
                    return -1;
                }
            } catch (RemoteException e) {
                DLog.d("remote exception happen in 1 " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }
        DLog.d("open file local 2");
        while(keep) {
            for (IFileWorkerService m : mFileWorkerServiceArray) {
                if (m != null) {
                    try {
                        DLog.d("open file local 3");
                        if(m.openFile(name) >= 0) {
                            DLog.d("open file local 4");
                            mFileMap.put(name, m);
                            mCachedWorkerService = m;
                            //DLog.d(name + "open pass");
                            return 0;
                        }else if(m.available()){
                            DLog.d(name + "open fail but avaiable");
                            return -1;
                        }
                    } catch (RemoteException e) {
                        DLog.d("remote exception happen in 2 " + e.getMessage());
                        e.printStackTrace();
                        return -1;
                    }

                }
            }
            DLog.d("start active new service");
            keep = findAvailableService();
            DLog.d("finish active new service");
        }
        return 0;
    }

    @Override
    public int backupFile(FileBean fb) {
        if(fb == null)
            return -1;

        mBackupMap.put(fb.getSrcPath(), fb);

        Message msg = mHandler.obtainMessage();
        msg.what = BACKUPFILE;
        msg.arg1 = fb.getType();
        Bundle bundle = new Bundle();
        bundle.putString("sname", fb.getSrcPath());
        bundle.putString("dname", fb.getBackupPath());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    private int backupFileLocal(String sname, String dname, int type) {
        int ret = 0;
        IFileWorkerService m = mFileMap.get(sname);
        try {
            if(m == null)
                return -1;

            ret = m.backupFile(sname, dname, type);
            mFileMap.remove(sname);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public int recoverFile(FileBean fb) {
        if(fb == null)
            return -1;

        mRecoverMap.put(fb.getBackupPath(), fb);

        Message msg = mHandler.obtainMessage();
        msg.what = RECOVERFILE;
        msg.arg1 = fb.getType();
        Bundle bundle = new Bundle();
        bundle.putString("sname", fb.getBackupPath());
        bundle.putString("dname", fb.getSrcPath());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    private int recoverFileLocal(String sname, String dname, int type) {
        try {
            int hasRead;
            DLog.d("file " + sname + dname + " recover in file worker.");
            FileInputStream in = null;
            FileOutputStream out = null;
            File fileIn = new File(sname);
            in = new FileInputStream(fileIn);
            File fileOut = new File(dname);
            fileOut.createNewFile();

            out = new FileOutputStream(fileOut);
            byte[] buff = new byte[1024];

            while( (hasRead = in.read(buff)) > 0){
                out.write(buff, 0, hasRead);
            }
            in.close();
            out.close();

            if(type == FileBean.TYPE.IMG || type == FileBean.TYPE.VIDEO)
                refreshSystemAlbum(dname);

            fileIn.delete();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    private IFileWorkerListener.Stub mFileWorkerListener = new IFileWorkerListener.Stub(){
        @Override
        public void onFileOpened(String name, int retval) throws RemoteException {
            DLog.d(name + "File opened cb from remote server");
        }

        @Override
        public void onFileBackuped(String name, int retval) throws RemoteException {
            DLog.d(name + "File backuped cb from remote server");
        }
    };

    private ServiceConnection workerserviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // TODO Auto-generated method stub
            DLog.d("worker wervice " + className + " connected.");
            int n = getClassNum(className.getClassName());
            IFileWorkerService fileWorkerService = IFileWorkerService.Stub.asInterface(service);
            if(n != -1) {
                mFileWorkerServiceArray[n] = fileWorkerService;
                try {
                    mFileWorkerServiceArray[n].setFileWorkerListener(mFileWorkerListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            synchronized (token) {
                token.setFlag("liangyi" + Integer.toString(1));
                token.notifyAll();
                DLog.d("notify all process");
            }
            if(!mReady) {
                mReady = true;
                Message sent = mMainHandler.obtainMessage();
                sent.what = WORKREADY;
                mMainHandler.sendMessage(sent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // TODO Auto-generated method stub
            DLog.d("worker wervice " + className + " disconnected.");
            int n = getClassNum(className.getClassName());
            if(n != -1)
                mFileWorkerServiceArray[n] = null;
        }

    };

    private boolean findAvailableService() {
        int n;
        IFileWorkerService m;
        Intent intent = null;
        for(n = 0; n < WORKER_SERVICE_NUM; n++){
            m = mFileWorkerServiceArray[n];
            if(m == null){
                switch(n){
                    case 0:
                        intent = new Intent(mContext, FileWorkerService000.class);
                        break;
                    case 1:
                        intent = new Intent(mContext, FileWorkerService001.class);
                        break;
                    case 2:
                        intent = new Intent(mContext, FileWorkerService002.class);
                        break;
                    case 3:
                        intent = new Intent(mContext, FileWorkerService003.class);
                        break;
                    case 4:
                        intent = new Intent(mContext, FileWorkerService004.class);
                        break;
                    case 5:
                        intent = new Intent(mContext, FileWorkerService005.class);
                        break;
                    case 6:
                        intent = new Intent(mContext, FileWorkerService006.class);
                        break;
                    case 7:
                        intent = new Intent(mContext, FileWorkerService007.class);
                        break;
                    case 8:
                        intent = new Intent(mContext, FileWorkerService008.class);
                        break;
                    case 9:
                        intent = new Intent(mContext, FileWorkerService009.class);
                        break;
                    case 10:
                        intent = new Intent(mContext, FileWorkerService010.class);
                        break;
                    case 11:
                        intent = new Intent(mContext, FileWorkerService011.class);
                        break;
                    case 12:
                        intent = new Intent(mContext, FileWorkerService012.class);
                        break;
                    case 13:
                        intent = new Intent(mContext, FileWorkerService013.class);
                        break;
                    case 14:
                        intent = new Intent(mContext, FileWorkerService014.class);
                        break;
                    case 15:
                        intent = new Intent(mContext, FileWorkerService015.class);
                        break;
                    default:
                        break;
                }
                if(intent != null) {
                    DLog.d("start to bind num " + n + " server");
                    mContext.bindService(intent, workerserviceConn, Context.BIND_AUTO_CREATE);
                    synchronized (token) {
                        try {
                            DLog.d("wait for service run");
                            token.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private int getClassNum(String name){
        int n;
        String tmp = name.substring(name.length()-3, name.length());
        try {
            n = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            DLog.d("get class number error");
            n = -1;
        }
        return n;
    }

    private class Token {
        private String flag;
        public Token() {
            setFlag(null);
        }
        public void setFlag(String flag) {
            this.flag = flag;
        }
        public String getFlag() {
            return flag;
        }
    }

    private void refreshSystemAlbum(String path){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }
}

