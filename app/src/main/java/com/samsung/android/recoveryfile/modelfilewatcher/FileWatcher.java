package com.samsung.android.recoveryfile.modelfilewatcher;

import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.samsung.android.filerecycle.common.DLog;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by samsung on 2017/5/5.
 */

public class FileWatcher implements IFileWatcher {

    private FileObserver mFileObserver = null;
    private OnFileWatcherListener mFileWatcherListener = null;
    private WatcherThread mWatchThread;

    private Set<String> mWatcherPaths = new HashSet<>();
    private HashMap<String, HashMap<String, Integer>> mWatcherMaps = new HashMap<>();

    private MyMainThreadHandler mHandler;
    public static final int CREATEFILE         = 0x001;
    public static final int DELETEFILE         = 0x002;

    public FileWatcher(){
    }

    @Override
    public void setFileListener(OnFileWatcherListener m) {
        DLog.d("set on file change listener");
        mFileWatcherListener = m;
    }

    @Override
    public void setWatcherPath(String path) {
        DLog.d("set watcher path " + path);
        if(path != null) {
            File dir = new File(path);
            if(!dir.exists()){
                DLog.e("set watcher path " + path + "doesn't exist");
                return;
            }
            if(!mWatcherPaths.contains(path))
                mWatcherPaths.add(path);
        }
    }

    @Override
    public void clearWatcherPath(String path) {
        if(path != null) {
            if (mWatcherPaths.contains(path)) {
                mWatcherPaths.remove(path);
                mWatcherMaps.remove(path);
            }else{
                DLog.e("path " + path + " is not in file watcher list");
            }
        }
    }

    @Override
    public void start() {
        DLog.d("Build version " + Build.VERSION.SDK_INT);
//        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
//                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 ||
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            mFileObserver = new SdcardFileObserver(mWatcherPath);
//            mFileObserver.startWatching();
//        } else {
            mHandler = new MyMainThreadHandler(Looper.getMainLooper());
            mWatchThread = new WatcherThread("watcher_thread");
            mWatchThread.start();
//        }
        DLog.d("file water thread start watch.");
    }

    @Override
    public void stop() {
        DLog.d("file watcher service stopped");
    }

    class SdcardFileObserver extends FileObserver {
        public SdcardFileObserver(String path) {
            super(path);
            DLog.d("FileObserver start");
        }

        public SdcardFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            final int action = event & FileObserver.ALL_EVENTS;
            switch(action) {
                case FileObserver.CREATE:
                    DLog.d("onEvent: file " + path + " create");
                    mFileWatcherListener.onFileChange(path, OnFileWatcherListener.FILE_CREATE);
                    break;
                case FileObserver.DELETE:
                    DLog.d("onEvent: file " + path + " delete");
                    mFileWatcherListener.onFileChange(path, OnFileWatcherListener.FILE_DELETE);
                    break;
                default:
                    break;
            }
        }
    }

    private class WatcherThread extends Thread {
        private int mCount;
        WatcherThread(String name){
            super(name);
        }

        @Override
        public void run() {
            super.run();

            DLog.d("WatcherThread start");
            mCount = 0;

            for(String path : mWatcherPaths){
                File dir = new File(path);
                String[] fileName = dir.list();
                if(fileName == null)
                    continue;

                HashMap map = new HashMap<String, Integer>();
                for(String s : fileName){
                    map.put(s, mCount);
                }
                mWatcherMaps.put(path, map);
            }

            while(true){
                try {
                    sleep(2000);
                    DLog.d("scanning");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(String path : mWatcherPaths){
                    File dir = new File(path);
                    String[] fileName = dir.list();
                    if(fileName == null)
                        continue;

                    HashMap map = mWatcherMaps.get(path);
                    if(map == null){
                        map = new HashMap<String, Integer>();
                        for(String s : fileName){
                            map.put(s, mCount);
                        }
                        mWatcherMaps.put(path, map);
                        continue;
                    }

                    for(String s : fileName) {
                        Integer count = (Integer)map.get(s);
                        if(count == null){
                            DLog.d("onEvent: file " + path + s + " create");
                            map.put(s, mCount + 1);
                            Message msg = mHandler.obtainMessage();
                            msg.obj = path + s;
                            msg.what = CREATEFILE;
                            mHandler.sendMessage(msg);
                        }else if(count == mCount){
                            map.put(s, mCount + 1);
                        }
                    }

                    Iterator<String> it = map.keySet().iterator();
                    while(it.hasNext()){
                        String s = it.next();
                        Integer v = (Integer)map.get(s);
                        if(v == mCount){
                            DLog.d("onEvent: file " + path + s + " delete");
                            it.remove();
                            Message msg = mHandler.obtainMessage();
                            msg.obj = path + s;
                            msg.what = DELETEFILE;
                            mHandler.sendMessage(msg);
                        }
                    }
                }

                mCount++;
                if(mCount==999999999)mCount = 0;
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
                    mFileWatcherListener.onFileChange(name, OnFileWatcherListener.FILE_CREATE);
                    break;
                case DELETEFILE:
                    mFileWatcherListener.onFileChange(name, OnFileWatcherListener.FILE_DELETE);
                    break;
                default:
                    break;
            }
        }
    }
}
