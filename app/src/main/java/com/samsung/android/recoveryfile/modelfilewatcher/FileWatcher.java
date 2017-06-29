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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by samsung on 2017/5/5.
 */

public class FileWatcher implements IFileWatcher {

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

    private FileObserver mFileObserver = null;
    private OnFileWatcherListener mFileWatcherListener = null;
    private WatcherThread mWatchThread;
    private Map<String, WatchDirs> mWatcherDirs = Collections.synchronizedMap(new HashMap<String, WatchDirs>());
    private int[] mWatchType = new int[FileBean.TYPE.TOTAL];
    public static final int FILE_WATCHER_DEPTH = 3;
    private static final int UNWATCH   = 0x00;
    private static final int TOWATCH   = 0x01;
    private static final int WATCHED   = 0x02;
    private static final int TOUNWATCH = 0x03;

    private MyMainThreadHandler mHandler;
    private static final int CREATEFILE         = 0x001;
    private static final int DELETEFILE         = 0x002;
    private static final int CLOSEFILE          = 0x003;

    private class WatchDirs {
        Map<String, Integer> maps = null;
        int depth;
        boolean isDir;
        public WatchDirs(int level, boolean dir){
            depth = level;
            isDir = dir;
        }
    }

    public FileWatcher(){
        super();
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++)
            mWatchType[i] = TOWATCH;

        DLog.d("filewatcher oncreate.");
    }

    @Override
    public void start() {
        DLog.d("filewatcher start.");
        for(String s : WATCHER_PATHS){
            setWatcherPath(s, FILE_WATCHER_DEPTH);
        }
        //special treatment for root.
        setWatcherPath(Environment.getExternalStorageDirectory().getPath()+"/", 1);

        mHandler = new MyMainThreadHandler(Looper.getMainLooper());
        mWatchThread = new WatcherThread("watcher_thread");
        mWatchThread.start();
    }

    @Override
    public void stop() {
        DLog.d("filewatcher stopped.");
    }

    @Override
    public void setFileListener(OnFileWatcherListener m) {
        DLog.d("filewatcher set on file change listener");
        mFileWatcherListener = m;
    }

    @Override
    public void setWatcherPath(String path, int depth) {
        DLog.d("filewatcher set watcher path:" + path);
        if(path != null) {
            File dir = new File(path);
            if(!dir.exists()){
                DLog.e("filewatcher set watcher path:" + path + " doesn't exist");
                return;
            }

            boolean isDir = dir.isDirectory();

            if(!mWatcherDirs.containsKey(path)) {
                mWatcherDirs.put(path, new WatchDirs(depth, isDir));
            }

            if(depth > 0 && isDir){
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
    public void clearWatcherPath(String path) {
        if(path != null) {
            if (mWatcherDirs.containsKey(path)) {
                mWatcherDirs.remove(path);
            }else{
                DLog.e("filewatcher path " + path + " is not in file watcher list");
            }
        }
    }

    @Override
    public void setWatchType(int type, boolean watch) {
        if(type < 0 || type >= FileBean.TYPE.TOTAL){
            DLog.d("filewatcher set type error:" + type);
            return;
        }

        int todo;
        if(watch) {
            todo = TOWATCH;
            DLog.d("filewatch set type " + type + " watch");
        } else {
            todo = TOUNWATCH;
            DLog.d("filewatch set type " + type + " unwatch");
        }

        if((todo == TOWATCH && (mWatchType[type] == WATCHED || mWatchType[type] == TOWATCH))
                || todo == TOUNWATCH && (mWatchType[type] == UNWATCH  || mWatchType[type] == TOUNWATCH)){
            DLog.d("filewatcher watcher type is already set.");
            return;
        }

        mWatchType[type] = todo;
    }

    @Override
    public void startFullScan() {
        DLog.d("filewatcher start full scan.");
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                int cnt = 0;
                Iterator it = mWatcherDirs.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry entry = (Map.Entry)it.next();
                    String path = (String)entry.getKey();
                    WatchDirs dir = (WatchDirs)entry.getValue();

                    if(!dir.isDir){
                        DLog.d("filewatcher come " + path);
                        int type = FileBean.fileTypeFilter(path);
                        if(type >= 0){
                            if(mWatchType[type] == TOWATCH){
                                DLog.d("filewatcher fullOnEvent open file:" + path);
                                openFileCb(path);
                            }else if(mWatchType[type] == TOUNWATCH){
                                DLog.d("filewatcher fullOnEvent close file:" + path);
                                closeFileCb(path);
                            }
                        }
                        continue;
                    }

                    File d = new File(path);
                    String[] fileName = d.list();
                    if(fileName == null)
                        continue;

                    for(String s : fileName){
                        int type = FileBean.fileTypeFilter(s);
                        if(type >= 0){
                            if(mWatchType[type] == TOWATCH){
                                DLog.d("filewatcher fullOnEvent open:" + path+s);
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
                                DLog.d("filewatcher fullOnEvent close:" + path+s);
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

    private class WatcherThread extends Thread {
        private int mCount;
        private Map<String, WatchDirs> addList = new HashMap<>();
        private Set<String> delList = new HashSet<>();
        WatcherThread(String name){
            super(name);
        }

        @Override
        public void run() {
            super.run();

            DLog.d("filewatcher thread start.");
            mCount = 0;

            while(true){
                try {
                    sleep(3000);
                    DLog.i("filewatcher scanning");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Iterator itor = mWatcherDirs.entrySet().iterator();
                while(itor.hasNext()){
                    Map.Entry entry = (Map.Entry)itor.next();
                    String path = (String)entry.getKey();
                    WatchDirs dir = (WatchDirs)entry.getValue();
                    Map map = dir.maps;
                    File d = new File(path);

                    if(!dir.isDir){
                        if(!d.exists()){
                            DLog.d("filewatcher file " + path + " has been deleted");
                            delList.add(path);
                        }
                        continue;
                    }

                    String[] fileName = d.list();
                    if(fileName == null) {
                        DLog.d("filewatcher dir " + path + " is become null possible has been deleted");
                        delList.add(path);
                        continue;
                    }

                    if(map == null){
                        DLog.d("filewatcher create new dir map " + path);
                        map = new HashMap<String, Integer>();
                        for(String s : fileName){
                            map.put(s, mCount + 1);
                            File tmp = new File(path + s);
                            if(!s.startsWith(".") && tmp.isDirectory() && dir.depth > 0){
                                DLog.d("filewatcher find new dir "+ path + s);
                                String subdir = path + s + "/";
                                addList.put(subdir, new WatchDirs(dir.depth-1, true));
                            }
                        }
                        dir.maps = map;
                        continue;
                    }

                    for(String s : fileName) {
                        Integer count = (Integer)map.get(s);
                        if(count == null){
                            int type = FileBean.fileTypeFilter(s);
                            DLog.d("filewatcher onEvent:file " + path + s + " create");
                            map.put(s, mCount + 1);
                            File tmp = new File(path + s);
                            if(!s.startsWith(".") && tmp.isDirectory() && dir.depth > 0){
                                DLog.d("filewatcher find new dir "+ path + s);
                                String subdir = path + s + "/";
                                addList.put(subdir, new WatchDirs(dir.depth-1, true));
                            }
                            if(type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                                openFileCb(path + s);
                        }else if(count == mCount){
                            map.put(s, mCount + 1);
                        }
                    }

                    Iterator it = map.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry ent = (Map.Entry)it.next();
                        String s = (String)ent.getKey();
                        Integer v = (Integer)ent.getValue();
                        if(v == mCount){
                            int type = FileBean.fileTypeFilter(s);
                            DLog.d("filewatcher onEvent:file " + path + s + " delete");
                            it.remove();
                            if(type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                                deletFileCb(path + s, type);
                        }
                    }
                }

                if(!delList.isEmpty()){
                    for(String path : delList){
                        WatchDirs dir = mWatcherDirs.get(path);
                        if(dir != null){
                            if(dir.isDir) {
                                DLog.d("filewatcher remove dir " + path);
                                if (dir.maps != null) {
                                    Iterator it = dir.maps.entrySet().iterator();
                                    while (it.hasNext()) {
                                        Map.Entry ent = (Map.Entry) it.next();
                                        String name = (String) ent.getKey();
                                        int type = FileBean.fileTypeFilter(name);
                                        DLog.d("filewatcher onEvent:file " + path + name + " delete");
                                        if (type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                                            deletFileCb(path + name, type);
                                    }
                                    dir.maps.clear();
                                    dir.maps = null;
                                }
                            }else{
                                DLog.d("filewatcher remove file " + path);
                                int type = FileBean.fileTypeFilter(path);
                                if (type >= 0 && (mWatchType[type] == TOWATCH || mWatchType[type] == WATCHED))
                                    deletFileCb(path, type);
                            }
                            mWatcherDirs.remove(path);
                        }
                    }
                }

                if(!addList.isEmpty()) {
                    mWatcherDirs.putAll(addList);
                    addList.clear();
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
}
