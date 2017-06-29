package com.samsung.android.recoveryfile.modelfilethumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by samsung on 2017/6/16.
 */

public class FileThumbnail implements IFileThumbnail {
    private OnFileThumbnailListener mFileThumbnailListener = null;
    private HashMap<String, FileBean> mCreateMap = new HashMap<>();
    private ExecutorService executorService;
    private MyMainThreadHandler mHandler;
    public static final int CREATTHUMBNAIL         = 0x001;

    public FileThumbnail(){
        super();
        DLog.d("filethumbnail oncreate.");
    }

    @Override
    public void start() {
        DLog.d("filethumbnail start.");
        executorService = Executors.newCachedThreadPool();
        mHandler = new MyMainThreadHandler(Looper.getMainLooper());
    }

    @Override
    public void stop() {

    }

    @Override
    public void setFileThumbnailListener(OnFileThumbnailListener m) {
        mFileThumbnailListener = m;
    }

    @Override
    public String getSysThumbnail(FileBean fb) {
        return null;
    }

    @Override
    public int createThumbnail(FileBean fb) {
        if(fb == null)
            return -1;

        DLog.d("filethumbnail create:" + fb.getSrcPath());
        if(fb.getType() != FileBean.TYPE.IMG)
            return -1;

        mCreateMap.put(fb.getBackupPath(), fb);
        MyImgCreateThumbnail job = new MyImgCreateThumbnail(fb.getBackupPath(), fb.getThumbnailPath());
        executorService.execute(job);

        return 0;
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
                case CREATTHUMBNAIL:
                    FileBean fb = mCreateMap.get(name);
                    mCreateMap.remove(name);
                    if(fb != null && mFileThumbnailListener != null){
                        mFileThumbnailListener.onFileThumbnailCreate(fb);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    class MyImgCreateThumbnail implements Runnable{
        private String inPath;
        private String OutPath;

        public MyImgCreateThumbnail(String in, String out){
            inPath = in;
            OutPath = out;
        }

        @Override
        public void run() {
            DLog.d("filethumbnail threadpoll create:" + inPath + " : " + OutPath);
            generateThumbnail(inPath, OutPath);
            Message msg = mHandler.obtainMessage();
            msg.obj = inPath;
            msg.what = CREATTHUMBNAIL;
            mHandler.sendMessage(msg);
        }
    }

    private void generateThumbnail(String inpath, String outPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(inpath, options);
        options.inJustDecodeBounds = false;

        DLog.d("filethumbnail w " + options.outWidth + " h " + options.outHeight);
        int be = (int)(options.outHeight / (float)200);
        if(be <= 0)
            be = 1;
        options.inSampleSize = be;

        DLog.d("filethumbnail decode file " + inpath);
        bitmap = BitmapFactory.decodeFile(inpath, options);
        if(bitmap==null) {
            DLog.e("filethumbnail inpath is not correct " + inpath);
            return;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        DLog.d("filethumbnail w " + w + " h " + h);

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        File file = new File(outPath);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)){
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
