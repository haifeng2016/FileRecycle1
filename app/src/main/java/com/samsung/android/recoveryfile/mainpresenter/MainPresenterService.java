package com.samsung.android.recoveryfile.mainpresenter;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.modelfiledb.FileNameDB;
import com.samsung.android.recoveryfile.modelfiledb.IFileNameDB;
import com.samsung.android.recoveryfile.modelfilethumbnail.FileThumbnail;
import com.samsung.android.recoveryfile.modelfilethumbnail.IFileThumbnail;
import com.samsung.android.recoveryfile.modelfilethumbnail.OnFileThumbnailListener;
import com.samsung.android.recoveryfile.modelfilewatcher.FileWatcher;
import com.samsung.android.recoveryfile.modelfilewatcher.IFileWatcher;
import com.samsung.android.recoveryfile.modelfilewatcher.OnFileWatcherListener;
import com.samsung.android.recoveryfile.modelfileworker.FileWorker;
import com.samsung.android.recoveryfile.modelfileworker.IFileWorker;
import com.samsung.android.recoveryfile.modelfileworker.OnFileWorkerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by samsung on 2017/6/9.
 */

public class MainPresenterService extends Service implements IMainPresenter {

    private static String APP_CACHE_PATH;
    private Set<String> fileWatcherPaths = new HashSet<>();
    private static final String[] FILE_WATCHER_PATHS = new String[] {
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath()+"/",
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getAbsolutePath()+"/",
             Environment.getExternalStorageDirectory().getPath()+"/backups/",
             Environment.getExternalStorageDirectory().getPath()+"/",
    };

    private IFileWatcher mIFileWatcher = null;
    private IFileWorker mIFileWorker = null;
    private IFileNameDB mIFileNameDB = null;
    private IFileThumbnail mIFileThumbnail = null;
    private OnMainPresenterListener mMainPresenterListner = null;
    private final IBinder binder = new MainPresenterBinder();
    private Object objSync = new Object();

    private ArrayList<ArrayList<FileBean>> mFileLists = new ArrayList<ArrayList<FileBean>>();

    private boolean mReady = false;
    private int mOpenFileCnt = 0;
    private int mRecoverFileCnt = 0;

    private boolean mOpenFileFinish = false;
    private boolean mOpenInitFinished = false;
    private boolean mRecoverFileFinish = false;

    public class MainPresenterBinder extends Binder {
        public MainPresenterService getService() {
            return MainPresenterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        APP_CACHE_PATH = context.getCacheDir().getAbsolutePath();
        File dir;

        for(int i = 0; i < FileBean.TYPE.TOTAL; i++) {
            dir = new File(APP_CACHE_PATH + FileBean.getTypeName(i));
            dir.mkdir();
        }
        dir = new File(APP_CACHE_PATH + "thumbnails");
        dir.mkdir();

        mIFileNameDB = new FileNameDB(context);
        mIFileNameDB.start();

        initFileList();

        mIFileWatcher = new FileWatcher();
        addWatcherPath();
        mIFileWatcher.setFileListener(new MainOnFileWatcherListener());
        mIFileWatcher.start();

        mIFileThumbnail = new FileThumbnail();
        mIFileThumbnail.setFileThumbnailListener(new MainOnFileThumbnailListener());
        mIFileThumbnail.start();

        mIFileWorker = new FileWorker();
        mIFileWorker.start(context, new MainOnFileWorkerListener());
    }

    private void initFileList() {
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++) {
            ArrayList<FileBean> list = (ArrayList<FileBean>) mIFileNameDB.getFileName(i);

            if (list == null)
                list = new ArrayList<FileBean>();

            mFileLists.add(list);
        }
    }

    private void addWatcherPath() {
        for(String s : FILE_WATCHER_PATHS){
            DLog.d("add watch path:" + s);
            fileWatcherPaths.add(s);
            mIFileWatcher.setWatcherPath(s);
        }

        String columns[] = new String[] {
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATA,
        };

        ContentResolver cr = getContentResolver();;
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media.DATE_MODIFIED);

        if(cursor.moveToFirst()){
            Set<Integer> set = new HashSet<Integer>();
            int bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int photoPathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do{
                int bucketId = cursor.getInt(bucketIdIndex);
                String path = cursor.getString(photoPathIndex);
                if(!set.contains(bucketId)) {
                    set.add(bucketId);
                    if(path != null){
                        String parent = path.substring(0, path.lastIndexOf("/")+1);
                        if(!fileWatcherPaths.contains(parent)) {
                            DLog.d("add albume watch path:" + parent);
                            fileWatcherPaths.add(parent);
                            mIFileWatcher.setWatcherPath(parent);
                        }
                    }
                }
            }while(cursor.moveToNext());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        DLog.d("MainPresenterService on binde");
        return binder;
    }

    @Override
    public int start() {
        DLog.d("MainPresenterService start");
        return 0;
    }

    @Override
    public int stop() {
        return 0;
    }

    @Override
    public boolean isReady() {
        DLog.d("is ready " + mReady);
        return mReady;
    }

    @Override
    public List<FileBean> getBackupFiles(int type) {
        return mFileLists.get(type);
    }

    @Override
    public int recoverFiles(List<FileBean> list) {
        mRecoverFileFinish = false;
        for(FileBean m : list){
            if(m == null)
                continue;

            DLog.d("recover " + m.getUuid() + " " + m.getSrcPath());
            DLog.d("fb" + m.getName() + " " + m.getDelTime().toString() + " " + m.getSize());
            mIFileNameDB.delteFileName(m);
            mFileLists.get(m.getType()).remove(m);
            mRecoverFileCnt++;
            mIFileWorker.recoverFile(m);
            if(m.getType() == FileBean.TYPE.IMG) {
                File f = new File(m.getThumbnailPath());
                f.delete();
            }
        }
        mRecoverFileFinish = true;
        return 0;
    }

    @Override
    public int deleteFiles(List<FileBean> list) {
        for(FileBean m : list){
            if(m == null)
                continue;

            DLog.d("deleteFile " + m.getUuid() + " " + m.getSrcPath());
            mIFileNameDB.delteFileName(m);
            mFileLists.get(m.getType()).remove(m);
            File f = new File(m.getBackupPath());
            f.delete();
            if(m.getType() == FileBean.TYPE.IMG) {
                f = new File(m.getThumbnailPath());
                f.delete();
            }
        }
        if(mMainPresenterListner != null)
            mMainPresenterListner.onDeleteFiles(0);
        return 0;
    }

    @Override
    public int setFileWatcherType(String[] type) {
        return 0;
    }

    @Override
    public int setAutoDeleteTime(int time) {
        return 0;
    }

    public static String getFileBackupDir() {
        return APP_CACHE_PATH;
    }

    @Override
    public int setPresenterListener(OnMainPresenterListener listener) {
        mMainPresenterListner = listener;
        return 0;
    }

    class MainOnFileWatcherListener implements OnFileWatcherListener {
        @Override
        public void onFileChange(String path, int mode) {
            int type = FileBean.fileTypeFilter(path);
            if(type == -1)
                return;

            switch (mode){
                case OnFileWatcherListener.FILE_CREATE:
                    DLog.d("main present file create");
                    mIFileWorker.openFile(path);
                    break;
                case OnFileWatcherListener.FILE_DELETE:
                    final FileBean fb = new FileBean(type, path, new GregorianCalendar().getTime());
                    DLog.d("fb" + fb.getName() + " " + fb.getDelTime().toString() + " " + fb.getSize());
                    mIFileWorker.backupFile(fb);
                    mIFileNameDB.saveFileName(fb);
                    mFileLists.get(fb.getType()).add(fb);
                    DLog.d("backup file " + fb.getBackupPath());
                    break;
            }
        }
    }

    class MainOnFileThumbnailListener implements OnFileThumbnailListener{
        @Override
        public void onFileThumbnailCreate(FileBean fb) {
            DLog.d("Thumbnail create " + fb.getThumbnailPath());
            if(mMainPresenterListner != null) {
                mMainPresenterListner.onBackupFiles(fb);
            }
        }
    }

    class MainOnFileWorkerListener implements OnFileWorkerListener {
        @Override
        public void onReady() {
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    mOpenFileFinish = false;
                    for(String path : fileWatcherPaths) {
                        File dir = new File(path);
                        if (dir != null && dir.exists()) {
                            String[] names = dir.list();
                            if (names != null) {
                                for (String s : names) {
                                    int type = FileBean.fileTypeFilter(s);
                                    if(type != -1) {
                                        mIFileWorker.openFile(path + s);
                                        DLog.d("open file in main present");
                                        synchronized(objSync){
                                            mOpenFileCnt++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mOpenFileFinish = true;
                    return null;
                }

            }.execute();

            mReady = true;
            if(mMainPresenterListner != null)
                mMainPresenterListner.onReady(mReady);
        }

        @Override
        public void onFileOpened(String name, int retval) {
            synchronized(objSync){
                mOpenFileCnt--;
                DLog.d("onFileOpened " + mOpenFileCnt);
                if(mOpenFileFinish && mOpenFileCnt==0)
                    mReady = true;
            }
            if(mReady && !mOpenInitFinished && mMainPresenterListner!=null) {
                mOpenInitFinished = true;
            }
        }

        @Override
        public void onFileBackuped(FileBean fb, int retval) {
            DLog.d("onFileBackuped" + fb.getSrcPath());
            File f = new File(fb.getBackupPath());
            fb.setSize(f.length());
            if(fb.getType() == FileBean.TYPE.IMG) {
                mIFileThumbnail.createThumbnail(fb);
            } else {
                if(mMainPresenterListner != null) {
                    mMainPresenterListner.onBackupFiles(fb);
                }
            }

        }

        @Override
        public void onFileRecovered(FileBean fb, int retval) {
            mRecoverFileCnt--;
            if(mRecoverFileFinish && mRecoverFileCnt == 0)
                if(mMainPresenterListner != null)
                    mMainPresenterListner.onRecoverFiles(0);
        }
    }
}
