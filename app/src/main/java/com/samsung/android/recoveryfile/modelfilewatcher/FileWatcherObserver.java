package com.samsung.android.recoveryfile.modelfilewatcher;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.io.File;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by samsung on 2017/6/22.
 */

public class FileWatcherObserver implements IFileWatcher {

    private static final String[] WATCHER_PATHS = new String[] {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath()+"/",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getAbsolutePath()+"/",
    };

    private static final int UNWATCH   = 0x00;
    private static final int TOWATCH   = 0x01;
    private static final int WATCHED   = 0x02;
    private static final int TOUNWATCH = 0x03;

    private MyMainThreadHandler mHandler;
    private static final int CREATEFILE         = 0x001;
    private static final int DELETEFILE         = 0x002;
    private static final int CLOSEFILE          = 0x003;
    private static final int DELETESELF         = 0x004;
    private static final int ADDPATH            = 0x005;

    private OnFileWatcherListener mFileWatcherListener = null;
    private Map<String, FileObserverImpl> mWatcherMap = Collections.synchronizedMap(new HashMap<String, FileObserverImpl>());
    private int[] mWatchType = new int[FileBean.TYPE.TOTAL];

    public FileWatcherObserver(){
        super();
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++)
            mWatchType[i] = TOWATCH;

        DLog.d("fileobserver oncreate.");
    }

    @Override
    public void setWatcherPath(String path, int depth) {
        if(path != null) {
            File dir = new File(path);
            if(!dir.exists() || !dir.isDirectory()){
                DLog.e("fileobserver set watcher path:" + path + " is not correct");
                return;
            }
            
            if(!mWatcherMap.containsKey(path)) {
                FileObserverImpl observer = new FileObserverImpl(path, depth);
                observer.startWatching();
                mWatcherMap.put(path, observer);
                DLog.d("fileobserver set watcher path:" + path);
            }

            if(depth > 0){
                String[] fileName = dir.list();
                if(fileName == null)
                    return;
                for(String s : fileName){
                    File t = new File(path + s);
                    if(!s.startsWith(".") && t.isDirectory()){
                        setWatcherPath(path + s + "/", depth - 1);
                    }
                }
            }
        }
    }

    @Override
    public void setWatchType(int type, boolean watch) {

    }

    @Override
    public void clearWatcherPath(String path) {
        if(path != null) {
            if (mWatcherMap.containsKey(path)) {
                DLog.d("fileobserver clear watcher path:" + path);
                FileObserverImpl observer = mWatcherMap.get(path);
                observer.stopWatching();
                mWatcherMap.remove(path);
            }else{
                DLog.e("fileobserver path " + path + " is not in file watcher list");
            }
        }
    }

    @Override
    public void setFileListener(OnFileWatcherListener m) {
        DLog.d("fileobserver set on file change listener");
        mFileWatcherListener = m;
    }

    @Override
    public void start() {
        DLog.d("fileobserver start.");
        mHandler = new MyMainThreadHandler(Looper.getMainLooper());
        for(String s : WATCHER_PATHS){
            setWatcherPath(s, 3);
        }

        //special treatment for root.
        setWatcherPath(Environment.getExternalStorageDirectory().getPath()+"/", 1);
    }

    @Override
    public void startFullScan() {
        DLog.d("fileobserver start full scan.");
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                int cnt = 0;
                Iterator it = mWatcherMap.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry entry = (Map.Entry)it.next();
                    String path = (String)entry.getKey();

                    File dir = new File(path);
                    String[] fileName = dir.list();
                    if(fileName == null)
                        continue;

                    for(String s : fileName){
                        int type = FileBean.fileTypeFilter(s);
                        if(type >= 0){
                            if(mWatchType[type] == TOWATCH){
                                DLog.d("fileobserver full OnEvent open:" + path + s);
                                openFileCb(path + s);
                                cnt++;
                                if(cnt >= 100) {
                                    try {
                                        Thread.currentThread().sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    cnt = 0;
                                }
                            }else if(mWatchType[type] == TOUNWATCH){
                                DLog.d("fileobserver full OnEvent close:" + path + s);
                                closeFileCb(path + s);
                                if(cnt >= 100) {
                                    try {
                                        Thread.currentThread().sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    cnt = 0;
                                }
                            }
                        }
                    }
                }

                for(int i = 0; i < FileBean.TYPE.TOTAL; i++){
                    if(mWatchType[i] == TOWATCH){
                        mWatchType[i] = WATCHED;
                    }else if(mWatchType[i] == TOUNWATCH){
                        mWatchType[i] = UNWATCH;
                    }
                }
                return null;
            }

        }.execute();
    }

    @Override
    public void stop() {

    }

    class FileObserverImpl extends FileObserver {
        private String mPath;
        private int mDepth;

        public FileObserverImpl(String path, int depth) {
            super(path, FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM | FileObserver.MOVED_TO);
            mDepth = depth;
            mPath = path;
            DLog.d("fileobserver FileObserver start");
        }

        public String getmPath() {
            return mPath;
        }

        public int getmDepth() {
            return mDepth;
        }

        @Override
        public void onEvent(int event, String path) {
            final int action = event & FileObserver.ALL_EVENTS;
            int type = FileBean.fileTypeFilter(mPath + path);
            switch(action) {
                case FileObserver.CREATE:
                    DLog.d("fileobserver onEvent: file " + mPath + path + " create " + mDepth);
                    File file1 = new File(mPath + path);
                    if(mDepth > 0 && !path.startsWith(".") && file1.isDirectory()){
                        addPathCb(mPath + path + "/", mDepth-1);
                    }
                    if(type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                        openFileCb(mPath + path);
                    break;
                case FileObserver.DELETE:
                    DLog.d("fileobserver onEvent: file " + mPath + path + " delete");
                    if(type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                        deletFileCb(mPath + path, type);
                    File file2 = new File(mPath + path);
                    if(!path.startsWith(".") && file2.isDirectory()){
                        deleteSelfFileCb(mPath + path + "/");
                    }
                    break;
                case FileObserver.DELETE_SELF:
                    DLog.d("fileobserver onEvent: file " + mPath + " selfdelete");
                    deleteSelfFileCb(mPath);
                    break;
                case FileObserver.MOVE_SELF:
                    DLog.d("fileobserver onEvent: file " + mPath + path + " MOVE_SELF");
                    break;
                case FileObserver.MOVED_FROM:
                    DLog.d("fileobserver onEvent: file " + mPath + path + " MOVED_FROM");
                    break;
                case FileObserver.MOVED_TO:
                    DLog.d("fileobserver onEvent: file " + mPath + path + " MOVED_TO");
                    File file3 = new File(mPath + path);
                    if(mDepth > 0 && !path.startsWith(".") && file3.isDirectory()){
                        addPathCb(mPath + path + "/", mDepth-1);
                    }
                    if(type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                        openFileCb(mPath + path);
                    break;
                default:
                    break;
            }
        }
    }

    class MyMainThreadHandler extends Handler {
        public MyMainThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            String name = (String)msg.obj;
            super.handleMessage(msg);
            switch(msg.what) {
                case CREATEFILE:
                    mFileWatcherListener.onFileChange(name, OnFileWatcherListener.FILE_CREATE, null);
                    break;
                case DELETEFILE:
                    int type = msg.arg1;
                    final FileBean fb = new FileBean(type, name, new GregorianCalendar().getTime());
                    mFileWatcherListener.onFileChange(name, OnFileWatcherListener.FILE_DELETE, fb);
                    break;
                case CLOSEFILE:
                    mFileWatcherListener.onFileChange(name, OnFileWatcherListener.FILE_CLOSE, null);
                    break;
                case ADDPATH:
                    int depth = msg.arg1;
                    setWatcherPath(name, depth);
                    File dir = new File(name);
                    String[] fileName = dir.list();
                    if(fileName != null){
                        for(String s : fileName) {
                            int t = FileBean.fileTypeFilter(s);
                            if(t >= 0) {
                                if(mWatchType[t] == TOWATCH){
                                    DLog.d("fileobserver addpath OnEvent open:" + name + s);
                                    mFileWatcherListener.onFileChange(name + s, OnFileWatcherListener.FILE_CREATE, null);
                                }
                            }else{
                                File file = new File(name + s);
                                if(depth > 0 && !s.startsWith(".") && file.isDirectory()){
                                    addPathCb(name + s + "/", depth-1);
                                }
                            }
                        }
                    }
                    break;
                case DELETESELF:
                    clearWatcherPath(name);
                default:
                    break;
            }
        }
    }

    private void openFileCb(String path){
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.what = CREATEFILE;
        mHandler.sendMessage(msg);
    }

    private void deletFileCb(String path, int type){
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.arg1 = type;
        msg.what = DELETEFILE;
        mHandler.sendMessage(msg);
    }

    private void closeFileCb(String path){
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.what = CLOSEFILE;
        mHandler.sendMessage(msg);
    }

    private void deleteSelfFileCb(String path){
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.what = DELETESELF;
        mHandler.sendMessage(msg);
    }

    private void addPathCb(String path, int depth){
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.arg1 = depth;
        msg.what = ADDPATH;
        mHandler.sendMessage(msg);
    }
}
