package com.samsung.android.recoveryfile.mainpresenter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.modelfiledb.FileNameDB;
import com.samsung.android.recoveryfile.modelfiledb.IFileNameDB;
import com.samsung.android.recoveryfile.modelfilethumbnail.FileThumbnail;
import com.samsung.android.recoveryfile.modelfilethumbnail.IFileThumbnail;
import com.samsung.android.recoveryfile.modelfilethumbnail.OnFileThumbnailListener;
import com.samsung.android.recoveryfile.modelfilewatcher.FileWatcher;
import com.samsung.android.recoveryfile.modelfilewatcher.FileWatcherObserver;
import com.samsung.android.recoveryfile.modelfilewatcher.IFileWatcher;
import com.samsung.android.recoveryfile.modelfilewatcher.OnFileWatcherListener;
import com.samsung.android.recoveryfile.modelfileworker.FileWorker;
import com.samsung.android.recoveryfile.modelfileworker.IFileWorker;
import com.samsung.android.recoveryfile.modelfileworker.OnFileWorkerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by samsung on 2017/6/9.
 */

public class MainPresenterService extends Service implements IMainPresenter {

    private static String APP_CACHE_PATH;
    private static IFileWatcher mIFileWatcher = null;
    private static IFileWorker mIFileWorker = null;
    private static IFileNameDB mIFileNameDB = null;
    private static IFileThumbnail mIFileThumbnail = null;
    private long mFileReserverTime = -1;
    private OnMainPresenterListener mMainPresenterListner = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final IBinder binder = new MainPresenterBinder();
    private static PackageManager mPackageManager = null;
    private static Map<String, String>mPackageMap = new HashMap<>();

    private ArrayList<ArrayList<FileBean>> mFileLists = new ArrayList<ArrayList<FileBean>>();

    private boolean mReady = false;
    private int mRecoverFileCnt = 0;
    private boolean mRecoverFileFinish = false;

    private static final int PACKAGE_ADD      = 0x01;
    private static final int PACKAGE_REMOVE   = 0x02;

    public class MainPresenterBinder extends Binder {
        public MainPresenterService getService() {
            return MainPresenterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DLog.d("mpresent oncreate.");
        Context context = getApplicationContext();
        APP_CACHE_PATH = context.getCacheDir().getAbsolutePath();
        mPackageManager = getPackageManager();

        mFileReserverTime = -1;
        File dir;
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++) {
            dir = new File(APP_CACHE_PATH + FileBean.getTypeName(i));
            dir.mkdir();
        }
        dir = new File(APP_CACHE_PATH + "thumbnails");
        dir.mkdir();

        mHandler.postDelayed(timeScheduleDelete, 2000);

        mIFileNameDB = new FileNameDB(context);
        mIFileNameDB.start();

        initFileList();

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mIFileWatcher = new FileWatcherObserver();
        } else {
            mIFileWatcher = new FileWatcher();
        }
        mIFileWorker = new FileWorker();
        mIFileWorker.start(context, new MainOnFileWorkerListener());

        mIFileWatcher.setFileListener(new MainOnFileWatcherListener());
        addWatcherPath();
        mIFileWatcher.start();

        mIFileThumbnail = new FileThumbnail();
        mIFileThumbnail.setFileThumbnailListener(new MainOnFileThumbnailListener());
        mIFileThumbnail.start();
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
                        DLog.d("mpresent add albume watch path:" + parent);
                        mIFileWatcher.setWatcherPath(parent, 1);
                    }
                }
            }while(cursor.moveToNext());
        }

        if(mPackageManager != null){
            List<ApplicationInfo> apps = mPackageManager.getInstalledApplications(0);
            if(apps != null){
                for(ApplicationInfo app : apps){
                    if((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                        DLog.d("mpresent add app " + app.sourceDir + " package name " + app.packageName);
                        //mIFileWatcher.setWatcherPath(app.sourceDir, 0);
                        if(mPackageMap.get(app.packageName) == null) {
                            mPackageMap.put(app.packageName, app.sourceDir);
                            mIFileWorker.openFile(app.sourceDir);
                        }
                    }
                }
            }
        }
    }

    public static class MyInstalledReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getDataString();
            packageName = packageName.substring(packageName.lastIndexOf(":") + 1);
            if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
                DLog.d("mpresent broadcast apk installed: " + packageName);
                try {
                    ApplicationInfo info = mPackageManager.getApplicationInfo(packageName, 0);
                    if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        if (mPackageMap.get(info.sourceDir) == null) {
                            mPackageMap.put(packageName, info.sourceDir);
                            mIFileWorker.openFile(info.sourceDir);
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    DLog.d("mpresent broadcast apk name no found: " + packageName);
                }
            }else if(intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")){
                DLog.d("mpresent broadcast apk uninstalled: " + packageName);
                if(mPackageMap.get(packageName) != null) {
                    final FileBean fb = new FileBean(FileBean.TYPE.APP, mPackageMap.get(packageName), new GregorianCalendar().getTime(), packageName);
                    mIFileWorker.backupFile(fb);
                }
            }
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

            DLog.d("ui recover file:" + m.getSrcPath() + " " + m.getDelTime().toString() + " " + m.getSize());
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

            DLog.d("ui delete file:" + m.getSrcPath() + " " + m.getDelTime().toString() + " " + m.getSize());
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
    public int setFileWatcherType(int[] type, boolean[] set) {
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++){
            if(i >= type.length || i >= set.length)
                return 0;
            mIFileWatcher.setWatchType(type[i], set[i]);
            mIFileWatcher.startFullScan();
        }

        return 0;
    }

    @Override
    public int setAutoDeleteTime(long time) {
        mFileReserverTime = time;
        return 0;
    }

    @Override
    public int setPresenterListener(OnMainPresenterListener listener) {
        mMainPresenterListner = listener;
        if(mReady){
            mMainPresenterListner.onReady(mReady);
        }
        return 0;
    }

    class MainOnFileWatcherListener implements OnFileWatcherListener {
        @Override
        public int onFileChange(String path, int mode, FileBean fb) {
            int ret = 0;
            switch (mode){
                case OnFileWatcherListener.FILE_CREATE:
                    DLog.d("mpresent filewatch create cb:" + path);
                    ret = mIFileWorker.openFile(path);
                    break;
                case OnFileWatcherListener.FILE_DELETE:
                    DLog.d("mpresent filewatch delete cb:" + path + " " + fb.getDelTime().toString());
                    ret = mIFileWorker.backupFile(fb);
                    break;
                case OnFileWatcherListener.FILE_CLOSE:
                    DLog.d("mpresent filewatch close cb:" + path);
                    ret = mIFileWorker.closeFile(path);
                    break;
            }
            return ret;
        }
    }

    class MainOnFileWorkerListener implements OnFileWorkerListener {
        @Override
        public void onReady() {
            if(mReady)
                return;

            mIFileWatcher.startFullScan();
            mReady = true;

            if(mMainPresenterListner != null) {
                DLog.d("mpresent fileworker ready report to ui");
                mMainPresenterListner.onReady(mReady);
            }
        }

        @Override
        public void onFileOpened(String name, int retval) {
            DLog.d("mpresent filework open cb:" + name + " ret:" + retval);
        }

        @Override
        public void onFileBackuped(FileBean fb, int retval) {
            DLog.d("mpresent filework backup cb:" + fb.getSrcPath() + " ret:" + retval);
            if(retval < 0){
                DLog.d("mpresent filework backup failed:" + fb.getSrcPath());
                return;
            }

            File f = new File(fb.getBackupPath());
            fb.setSize(f.length());
            if(fb.getType() == FileBean.TYPE.IMG) {
                mIFileThumbnail.createThumbnail(fb);
            } else {
                mIFileNameDB.saveFileName(fb);
                mFileLists.get(fb.getType()).add(fb);
                if(mMainPresenterListner != null) {
                    mMainPresenterListner.onBackupFiles(fb);
                }
            }
        }

        @Override
        public void onFileRecovered(FileBean fb, int retval) {
            DLog.d("mpresent filework recover cb:" + fb.getSrcPath() + " ret:" + retval);
            mRecoverFileCnt--;
            if(mRecoverFileFinish && mRecoverFileCnt == 0)
                if(mMainPresenterListner != null)
                    mMainPresenterListner.onRecoverFiles(0);
        }
    }

    class MainOnFileThumbnailListener implements OnFileThumbnailListener{
        @Override
        public void onFileThumbnailCreate(FileBean fb) {
            DLog.d("mpresent Thumbnail createcb:" + fb.getThumbnailPath());
            mIFileNameDB.saveFileName(fb);
            mFileLists.get(fb.getType()).add(fb);
            if(mMainPresenterListner != null) {
                mMainPresenterListner.onBackupFiles(fb);
            }
        }
    }

    private Runnable timeScheduleDelete = new Runnable() {
        @Override
        public void run() {
            if(mFileReserverTime > 0) {
                DLog.d("mpresent schedule del start.");
                boolean del = false;
                Date current = new GregorianCalendar().getTime();
                long time = current.getTime();
                for (List<FileBean> list : mFileLists) {
                    for (FileBean fb : list) {
                        if ((time - fb.getDelTime().getTime()) > mFileReserverTime){
                            del = true;
                            DLog.d("mpresent schedule delete file:" + fb.getSrcPath() + " " + fb.getDelTime().toString() + " " + fb.getSize());
                            mIFileNameDB.delteFileName(fb);
                            list.remove(fb);
                            File f = new File(fb.getBackupPath());
                            f.delete();
                            if(fb.getType() == FileBean.TYPE.IMG) {
                                f = new File(fb.getThumbnailPath());
                                f.delete();
                            }
                        }
                    }
                }

                if(mMainPresenterListner != null && del)
                    mMainPresenterListner.onDeleteFiles(0);
            }
            mHandler.postDelayed(this, 60000);
        }
    };

    public static String getFileBackupDir() {
        return APP_CACHE_PATH;
    }
}
