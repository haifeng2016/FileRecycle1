package com.samsung.android.recoveryfile.modelfileworker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by samsung on 2017/5/9.
 */

public class FileWorker implements IFileWorker {
    private final static int WORKER_SERVICE_NUM = 16;
    public static final int TOTAL_FD_NUM        = 900;
    private FileWorkerServcies[] mFileWorkerServices = new FileWorkerServcies[WORKER_SERVICE_NUM];
    private HashMap<String, Integer> mFileMap = new HashMap<>();
    private HashMap<String, FileBean> mBackupMap = new HashMap<>();
    private HashMap<String, FileBean> mRecoverMap = new HashMap<>();
    private OnFileWorkerListener mOnFileWorkerListener = null;
    private Context mContext;
    private Handler mWorkerHandler;
    private Handler mMainHandler;
    private HandlerThread mHanderThread;
    private boolean mReady = false;
    private Object syncBindServer = new Object();

    public static final int OPENFILE         = 0x001;
    public static final int BACKUPFILE       = 0x002;
    public static final int RECOVERFILE      = 0x003;
    public static final int SERVERREADY      = 0x004;
    public static final int CLOSEFILE        = 0x005;
    public static final int STARTSERVICE     = 0x006;

    public static final int SERVICESTOP      = 0x01;
    public static final int SERVICESTARTING  = 0x02;
    public static final int SERVICESTARTED   = 0x03;

    private class FileWorkerServcies {
        AtomicInteger openCnt;
        IFileWorkerService service;
        Set<String> openFiles;
        int ready;
    }

    @Override
    public void start(Context context, OnFileWorkerListener m) {
        DLog.d("fileworker start.");

        for(int i = 0; i < WORKER_SERVICE_NUM; i++){
            mFileWorkerServices[i] = new FileWorkerServcies();
            synchronized (syncBindServer) {
                mFileWorkerServices[i].openCnt = new AtomicInteger(0);
                mFileWorkerServices[i].service = null;
                mFileWorkerServices[i].openFiles = new HashSet<>();
                mFileWorkerServices[i].ready = SERVICESTOP;
            }
        }

        this.mContext = context;
        this.mOnFileWorkerListener = m;

        mHanderThread = new HandlerThread("worker_thread");
        mHanderThread.start();
        mWorkerHandler = new workerHandler(mHanderThread.getLooper());
        mMainHandler = new MyMainThreadHandler(Looper.getMainLooper());

        Message msg = mMainHandler.obtainMessage();
        msg.what = SERVERREADY;
        msg.arg1 = -1;
        mMainHandler.sendMessage(msg);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isReady() {
        return mReady;
    }

    @Override
    public int openFile(String name) {
        int select = -1;
        int i;

        if(name == null){
            DLog.d("fileworker open name is " + name);
            return -1;
        }

        if(mFileMap.get(name) != null){
            DLog.d("fileworker already opened this file:" + name);
            return 0;
        }

        for (i=0; i < WORKER_SERVICE_NUM; i++){
            if(mFileWorkerServices[i].openCnt.get() < TOTAL_FD_NUM){
                mFileWorkerServices[i].openCnt.incrementAndGet();
                break;
            }
        }

        if(i == WORKER_SERVICE_NUM){
            DLog.d("fileworker no more fd available now.");
            return -1;
        }

        mFileMap.put(name, i);

        synchronized (syncBindServer){
            mFileWorkerServices[i].openFiles.add(name);

            if(mFileWorkerServices[i].ready != SERVICESTARTED){
                if(mFileWorkerServices[i].ready == SERVICESTOP) {
                    mFileWorkerServices[i].ready = SERVICESTARTING;
                    startWorkerService(i);
                }
                return 0;
            }
        }

        Message msg = mWorkerHandler.obtainMessage();
        msg.what = OPENFILE;
        msg.obj = name;
        msg.arg1 = i;
        mWorkerHandler.sendMessage(msg);

        return 0;
    }

    @Override
    public int closeFile(String name) {
        if(name == null)
            return -1;

        Integer n = mFileMap.get(name);
        if(n == null) {
            DLog.d("fileworker close could not find mapped name:" + name);
            return -1;
        }
        mFileMap.remove(name);
        synchronized (syncBindServer){
            mFileWorkerServices[n].openFiles.remove(name);
        }

        Message msg = mWorkerHandler.obtainMessage();
        msg.what = CLOSEFILE;
        msg.obj = name;
        msg.arg1 = n;
        mWorkerHandler.sendMessage(msg);

        return 0;
    }

    @Override
    public int backupFile(FileBean fb) {
        if(fb == null)
            return -1;

        mBackupMap.put(fb.getSrcPath(), fb);

        Integer n = mFileMap.get(fb.getSrcPath());
        if(n == null) {
            DLog.d("fileworker backup could not find mapped name:" + fb.getSrcPath());
            return -1;
        }
        mFileMap.remove(fb.getSrcPath());
        synchronized (syncBindServer){
            mFileWorkerServices[n].openFiles.remove(fb.getSrcPath());
        }

        Message msg = mWorkerHandler.obtainMessage();
        msg.what = BACKUPFILE;
        msg.arg1 = n;
        msg.arg2 = fb.getType();
        Bundle bundle = new Bundle();
        bundle.putString("sname", fb.getSrcPath());
        bundle.putString("dname", fb.getBackupPath());
        msg.setData(bundle);
        mWorkerHandler.sendMessage(msg);

        return 0;
    }

    @Override
    public int recoverFile(FileBean fb) {
        if(fb == null)
            return -1;

        mRecoverMap.put(fb.getBackupPath(), fb);

        Message msg = mWorkerHandler.obtainMessage();
        msg.what = RECOVERFILE;
        msg.arg1 = fb.getType();
        Bundle bundle = new Bundle();
        bundle.putString("sname", fb.getBackupPath());
        bundle.putString("dname", fb.getSrcPath());
        msg.setData(bundle);
        mWorkerHandler.sendMessage(msg);
        return 0;
    }

    private int openFileLocal(String name, int n) {
        IFileWorkerService m = null;

        synchronized (syncBindServer) {
            if(mFileWorkerServices[n].ready != SERVICESTARTED) {
                DLog.e("fileworker service " + n + " is not started yet.");
                return -1;
            }
            m = mFileWorkerServices[n].service;
        }

        if(m == null)
            return -1;

        try {
            if(m.openFile(name) >= 0) {
                return 0;
            }else if(m.available()){
                DLog.d("fileworker open fail but avaiable2:" + name);
                return -1;
            }
        } catch (RemoteException e) {
            DLog.d("fileworker remote exception happen2 " + e.getMessage());
            return -1;
        }

        return -1;
    }

    private int closeFileLocal(String name, int n){
        int ret = 0;

        IFileWorkerService m = null;
        synchronized (syncBindServer){
            m = mFileWorkerServices[n].service;
        }

        if(m == null){
            DLog.e("fileworker close file servie is not ready");
            return -1;
        }

        try{
            ret = m.closeFile(name);
            mFileWorkerServices[n].openCnt.decrementAndGet();
        } catch(RemoteException e){
            DLog.e("fileworker close file from service error " + e.getMessage());
            return -1;
        }
        return ret;
    }

    private int backupFileLocal(String sname, String dname, int n) {
        DLog.d("fileworker backup file:" + sname + " : " + dname);

        IFileWorkerService m = null;
        int ready = SERVICESTOP;

        while(ready != SERVICESTARTED){
            synchronized (syncBindServer){
                ready = mFileWorkerServices[n].ready;
                m = mFileWorkerServices[n].service;
            }
            try {
                Thread.currentThread().sleep(50);
            } catch (InterruptedException e) {
                DLog.d("fileworker backupFileLocal wait error.");
            }
        }

        if(m == null){
            DLog.e("fileworker backup file servie is not ready");
            return -1;
        }

        int ret = 0;
        try {
            ret = m.backupFile(sname, dname);
            mFileWorkerServices[n].openCnt.decrementAndGet();
        } catch (RemoteException e) {
            DLog.e("fileworker backup file from service error " + e.getMessage());
            return -1;
        }
        return ret;
    }

    private int recoverFileLocal(String sname, String dname) {
        int ret = 0;
        boolean retry = true;
        while(retry) {
            try {
                int hasRead;
                DLog.d("fileworker recover file:" + sname + " : " + dname);

                String sub = dname.substring(dname.lastIndexOf("/") + 1);
                if(sub.equals("base.apk")){
                    String sub2 = dname.substring(0, dname.lastIndexOf("/") - 1);
                    String sub3 = sub2.substring(sub2.lastIndexOf("/"));
                    dname = Environment.getExternalStorageDirectory().getPath()+"/application/"+sub3+".apk";
                }

                FileInputStream in = null;
                FileOutputStream out = null;
                File fileIn = new File(sname);
                in = new FileInputStream(fileIn);
                File fileOut = new File(dname);
                if (fileOut.getParentFile() == null) {
                    DLog.d("fileworker recover path error " + dname);
                    return -1;
                }
                if (!fileOut.getParentFile().exists()) {
                    File dir = new File(fileOut.getParent());
                    dir.mkdirs();
                }
                fileOut.createNewFile();
                out = new FileOutputStream(fileOut);
                byte[] buff = new byte[1024];

                while ((hasRead = in.read(buff)) > 0) {
                    out.write(buff, 0, hasRead);
                }
                in.close();
                out.close();
                fileIn.delete();
                retry = false;
                ret = 0;
                int type = FileBean.fileTypeFilter(dname);
                if(type==FileBean.TYPE.IMG || type==FileBean.TYPE.VIDEO)
                    refreshSystemAlbum(dname);
            } catch (IOException e) {
                DLog.d("fileworker recover file:" + sname + " : " + dname + " error " + e.getMessage());
                if (e.getMessage().equals("Permission denied")) {
                    DLog.d("fileworker recover try to save to other place");
                    String lastName = dname.substring(dname.lastIndexOf("/") + 1);
                    dname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/"+lastName;
                    retry = true;
                }else{
                    ret = -1;
                    retry = false;
                }
            }
        }
        return ret;
    }

    private boolean startWorkerService(int n) {
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

        if(intent == null) {
            DLog.d("fileworker service select error " + n);
            return false;
        }

        DLog.d("fileworker start to bind num " + n + " server");
        mContext.bindService(intent, workerserviceConn, Context.BIND_AUTO_CREATE);
        return true;
    }

    private int getClassNum(String name){
        int n;
        String tmp = name.substring(name.length()-3, name.length());
        try {
            n = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            DLog.d("fileworker get class number error");
            n = -1;
        }
        return n;
    }

    private void refreshSystemAlbum(String path){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }

    private class workerHandler extends Handler {
        public workerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int n;
            String sname;
            String dname;

            switch(msg.what){
                case OPENFILE:
                    sname = (String)msg.obj;
                    n = msg.arg1;
                    openFileLocal(sname, n);
                    break;
                case BACKUPFILE:
                    sname = msg.getData().getString("sname");
                    dname = msg.getData().getString("dname");
                    n = msg.arg1;
                    backupFileLocal(sname, dname, n);
                    break;
                case RECOVERFILE:
                    sname = msg.getData().getString("sname");
                    dname = msg.getData().getString("dname");
                    int ret = recoverFileLocal(sname, dname);
                    Message sent = mMainHandler.obtainMessage();
                    sent.what = RECOVERFILE;
                    sent.obj = sname;
                    sent.arg1 = ret;
                    mMainHandler.sendMessage(sent);
                    break;
                case CLOSEFILE:
                    sname = (String)msg.obj;
                    n = msg.arg1;
                    closeFileLocal(sname, n);
                    break;
                default:
                    break;
            }
        }
    }

    private class MyMainThreadHandler extends Handler {
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
                    if(ret >= 0 && ret < WORKER_SERVICE_NUM) {
                        mOnFileWorkerListener.onFileOpened(name, ret);
                    }
                    break;
                case BACKUPFILE:
                    fb = mBackupMap.get(name);
                    mBackupMap.remove(name);
                    mOnFileWorkerListener.onFileBackuped(fb, ret);
                    break;
                case RECOVERFILE:
                    fb = mRecoverMap.get(name);
                    if(fb.getType() == FileBean.TYPE.IMG || fb.getType() == FileBean.TYPE.VIDEO)
                        refreshSystemAlbum(fb.getSrcPath());
                    mRecoverMap.remove(name);
                    mOnFileWorkerListener.onFileRecovered(fb, ret);
                    break;
                case SERVERREADY:
                    if(ret >= 0) {
                        Iterator<String> it;
                        synchronized (syncBindServer) {
                            it = mFileWorkerServices[ret].openFiles.iterator();
                            while (it.hasNext()) {
                                Message sent = mWorkerHandler.obtainMessage();
                                sent.what = OPENFILE;
                                sent.obj = it.next();
                                sent.arg1 = ret;
                                mWorkerHandler.sendMessage(sent);
                            }
                        }
                    }
                    mReady = true;
                    mOnFileWorkerListener.onReady();
                    break;
                case STARTSERVICE:
                    if(ret >= 0){
                        synchronized (syncBindServer){
                            mFileWorkerServices[ret].ready = SERVICESTARTING;
                            startWorkerService(ret);
                        }
                    }
                default:
                    break;
            }
        }
    }

    private IFileWorkerListener mFileWorkerListener = new IFileWorkerListener.Stub(){
        @Override
        public void onFileOpened(String name, int retval) throws RemoteException {
            DLog.d("fileworker file open cb:" + name + "from service:" + retval);
            Message sent = mMainHandler.obtainMessage();
            sent.what = OPENFILE;
            sent.arg1 = retval;
            sent.obj = name;
            mMainHandler.sendMessage(sent);
        }

        @Override
        public void onFileBackuped(String name, int retval) throws RemoteException {
            DLog.d("fileworker file backup cb:" + name + "from service:" + retval);
            Message sent = mMainHandler.obtainMessage();
            sent.what = BACKUPFILE;
            sent.arg1 = retval;
            sent.obj = name;
            mMainHandler.sendMessage(sent);
        }

        @Override
        public void onRemainFdCnt(int workIndex, int cnt) throws RemoteException {
        }
    };

    private ServiceConnection workerserviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // TODO Auto-generated method stub
            DLog.d("fileworker service " + className + " connected.");
            int n = getClassNum(className.getClassName());
            IFileWorkerService fileWorkerService = IFileWorkerService.Stub.asInterface(service);

            if(n < 0 || n >= WORKER_SERVICE_NUM){
                DLog.e("fileworker service connect get server index error " + n);
                return;
            }
            synchronized(syncBindServer){
                if(mFileWorkerServices[n].ready == SERVICESTARTED){
                    DLog.e("fileworker service " + n + " is already started");
                    return;
                }
                mFileWorkerServices[n].service = fileWorkerService;
                mFileWorkerServices[n].ready = SERVICESTARTED;
            }

            try {
                fileWorkerService.setFileWorkerListener(mFileWorkerListener);
                fileWorkerService.start();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Message msg = mMainHandler.obtainMessage();
            msg.what = SERVERREADY;
            msg.arg1 = n;
            mMainHandler.sendMessage(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // TODO Auto-generated method stub
            DLog.d("fileworker service " + className + " disconnected.");
            int n = getClassNum(className.getClassName());
            if(n != -1) {
                mFileWorkerServices[n].service = null;
                mFileWorkerServices[n].ready = SERVICESTOP;

                Message msg = mMainHandler.obtainMessage();
                msg.what = STARTSERVICE;
                msg.arg1 = n;
                mMainHandler.sendMessageDelayed(msg, 100);
            }
        }
    };
}

