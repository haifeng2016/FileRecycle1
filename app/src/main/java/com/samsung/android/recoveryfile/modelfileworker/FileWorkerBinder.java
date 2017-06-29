package com.samsung.android.recoveryfile.modelfileworker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.samsung.android.filerecycle.common.DLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by samsung on 2017/5/10.
 */

public class FileWorkerBinder extends IFileWorkerService.Stub {

    public static final int OPENFILE       = 0x001;
    public static final int BACKUPFILE     = 0x002;
    public static final int REMAINFD       = 0x003;
    public static final int CLOSEFILE      = 0x004;

    private HashMap<String, FileInputStream> fdHmap = new HashMap<String, FileInputStream>();
    private RemoteCallbackList<IFileWorkerListener> mCallbacks = new RemoteCallbackList<IFileWorkerListener>();
    private boolean mAvailable = true;
    private String mServiceName;
    private int mWorkIndex;
    private AtomicInteger mRemainFd = new AtomicInteger(FileWorker.TOTAL_FD_NUM);
    private Object syncFd = new Object();

    private Handler mMainHandler = new MyMainThreadHandler(Looper.getMainLooper());

    public FileWorkerBinder(String serviceName){
        super();
        mServiceName = serviceName;
        String tmp = mServiceName.substring(mServiceName.length()-3, mServiceName.length());
        DLog.d("filewservice " + mServiceName + " on create.");

        try {
            mWorkIndex = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            DLog.d("filewservice get class number error");
            mWorkIndex = 0;
        }
    }

    public void onCreate(){

    }

    public void onDestroy(){
        DLog.d("filewservice " + mWorkIndex + " onDestory");
        mMainHandler = null;
        Iterator itor = fdHmap.entrySet().iterator();
        while(itor.hasNext()) {
            Map.Entry entry = (Map.Entry) itor.next();
            String path = (String) entry.getKey();
            FileInputStream in = (FileInputStream) entry.getValue();
            try {
                in.close();
            } catch (IOException e) {
                DLog.d("filewservice " + path + " close failed" + e.getMessage());
            }
            itor.remove();
        }
    }

    public void onLowMemory(){

    }

    public boolean onUnbind(){
        return true;
    }

    public void onRebind(){

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DLog.d("filewservice " + mWorkIndex + " is finished");
    }

    @Override
    public void start() throws RemoteException {
        DLog.d("filewservice " + mServiceName + " start.");
    }

    @Override
    public void stop() throws RemoteException {
        DLog.d("filewservice " + mServiceName + " stop.");

    }

    class MyMainThreadHandler extends Handler {
        public MyMainThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int ret = 0;
            String sname;
            String dname;
            switch(msg.what) {
                case OPENFILE:
                    sname = (String)msg.obj;
                    ret = openFileLocal(sname);
                    onOpenCb(sname, ret);
                    break;
                case BACKUPFILE:
                    sname = msg.getData().getString("sname");
                    dname = msg.getData().getString("dname");
                    ret = backupFileLocal(sname, dname);
                    onBackupCb(sname, ret);
                    break;
                case REMAINFD:
                    onRemainFdCb();
                    break;
                case CLOSEFILE:
                    sname = (String)msg.obj;
                    ret = closeFileLocal(sname);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int openFile(String name) throws RemoteException {
        if(mRemainFd.get() <= 0)
            return -1;

        mRemainFd.decrementAndGet();
        Message msg = mMainHandler.obtainMessage();
        msg.what = OPENFILE;
        msg.obj = name;
        mMainHandler.sendMessage(msg);
        return 0;
    }

    private int openFileLocal(String name) {
        int ret = 0;
        DLog.d("filewservice open file:" + name + " in " + mServiceName);
        try {
            FileInputStream in = new FileInputStream(name);
            fdHmap.put(name, in);
            mAvailable = true;
        } catch (FileNotFoundException e) {
            DLog.d("filewservice open file failed:" + name + " in " + mServiceName + " " + e.getMessage());
            mAvailable = false;
            ret = -1;
        } catch (Exception e) {
            DLog.d("filewservice open file error:" + name + " in " + mServiceName + " " + e.getMessage());
            mAvailable = true;
            ret = -1;
        }

        return ret;
    }

    @Override
    public int backupFile(String sname, String dname) throws RemoteException {
        Message msg = mMainHandler.obtainMessage();
        msg.what = BACKUPFILE;
        Bundle bundle = new Bundle();
        bundle.putString("sname", sname);
        bundle.putString("dname", dname);
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
        return 0;
    }

    private int backupFileLocal(String sname, String dname){
        int ret = 0;
        FileInputStream in = null;
        FileOutputStream out = null;
        DLog.d("filewservice backup file:" + sname + " in " + mServiceName);
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

            fdHmap.remove(sname);
            mRemainFd.incrementAndGet();
            mAvailable = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            DLog.d("filewservice backup file failed:" + sname + " in " + mServiceName + " " + e.getMessage());
            ret = -1;
        }

        return ret;
    }

    @Override
    public int closeFile(String name) throws RemoteException {
        Message msg = mMainHandler.obtainMessage();
        msg.what = CLOSEFILE;
        msg.obj = name;
        mMainHandler.sendMessage(msg);

        return 0;
    }

    private int closeFileLocal(String name){
        int ret = 0;
        FileInputStream in = fdHmap.get(name);
        if(in == null){
            DLog.d("filewservice " + mWorkIndex + " could not find open file:" + name);
            return -1;
        }

        try {
            in.close();
            fdHmap.remove(name);
            mRemainFd.incrementAndGet();
            mAvailable = true;
        } catch (IOException e) {
            DLog.d("filewservice close file failed:" + name + " in " + mServiceName + " " + e.getMessage());
            ret = -1;
        }
        return ret;
    }

    @Override
    public int remainFdCnt() throws RemoteException {
        return 0;
    }

    @Override
    public String[] getOpenedFile() throws RemoteException {
        DLog.d("filewservice get open files");
//        if(fdHmap.size() == 0){
//            return null;
//        }
//        int i = 0;
//        String[] files = new String[fdHmap.size()];
//        Iterator iter = fdHmap.entrySet().iterator();
//        while(iter.hasNext()){
//            Map.Entry entry = (Map.Entry)iter.next();
//            files[i] = (String)entry.getKey();
//            i++;
//        }
//        return files;
        return null;
    }

    private int getProcessFdCnt(){
        int ret = 1024;
        int pid = android.os.Process.myPid();
        String fdDir = "/proc/" + pid + "/fd/";
        File dir = new File(fdDir);
        if(dir.exists() && dir.isDirectory()){
            ret -= dir.list().length;
        }else{
            DLog.e("read process fd count failed path not correct " + fdDir);
        }
        DLog.d("filewservice " + mWorkIndex + " remain fd cnt " + ret);
        return ret;
    }

    @Override
    public boolean available() throws RemoteException {
        return mAvailable && (mRemainFd.get() > 0);
    }

    @Override
    public void setFileWorkerListener(IFileWorkerListener cb) throws RemoteException {
        mCallbacks.register(cb);
        Message msg = mMainHandler.obtainMessage();
        msg.what = REMAINFD;
        mMainHandler.sendMessage(msg);
    }

    private void onOpenCb(String name, int val){
        final int n = mCallbacks.beginBroadcast();
        for(int i = 0; i < n; i++){
            try {
                mCallbacks.getBroadcastItem(i).onFileOpened(name, val);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }

    private void onBackupCb(String name, int val){
        final int n = mCallbacks.beginBroadcast();
        for(int i = 0; i < n; i++){
            try {
                mCallbacks.getBroadcastItem(i).onFileBackuped(name, val);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }

    private void onRemainFdCb(){
        final int n = mCallbacks.beginBroadcast();
        for(int i = 0; i < n; i++){
            try {
                mCallbacks.getBroadcastItem(i).onRemainFdCnt(mWorkIndex, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }
}