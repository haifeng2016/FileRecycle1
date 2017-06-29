package com.samsung.android.recoveryfile.modelfiledb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.modelfiledb.FileDBSchema.FileDBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samsung on 2017/5/10.
 */

public class FileNameDB implements IFileNameDB {
    private Context mContext;

    public FileNameDB(Context context) {
        DLog.d("filedb oncreate.");
        mContext = context;
    }

    @Override
    public void start() {
        DLog.d("filedb start.");
    }

    @Override
    public void stop() {
        DLog.d("filedb stop.");
    }

    @Override
    public int saveFileName(FileBean node) {
        DLog.d("filedb insert type:" + node.getType() + " name:" + node.getSrcPath());

        String tableName = node.typeName();
        if(tableName == null)
            return -1;

        ContentValues v = getContentValues(node);

        SQLiteDatabase db = new FileDBOpenHelper(mContext).getWritableDatabase();
        try {
            db.insert(tableName, null, v);
        } catch(SQLiteException e) {
            DLog.e(e.getMessage());
        } finally {
            db.close();
        }

        return 0;
    }

    @Override
    public int delteFileName(FileBean node) {
        DLog.d("filedb remove type:" + node.getType() + " name:" + node.getSrcPath());

        String tableName = node.typeName();
        if(tableName == null)
            return -1;

        SQLiteDatabase db = new FileDBOpenHelper(mContext).getWritableDatabase();
        try {
            db.delete(tableName, " uuid=?", new String[]{node.getUuid().toString()});
        } catch(SQLiteException e) {
            DLog.e(e.getMessage());
        } finally {
            db.close();
        }
        return 0;
    }

    private FileCursorWrapper queryFiles(String tableName, String whereClause, String[] whereArgs){
        SQLiteDatabase db = new FileDBOpenHelper(mContext).getReadableDatabase();
        Cursor cursor = db.query(tableName,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new FileCursorWrapper(cursor);
    }

    @Override
    public List<FileBean> getFileName(int type) {
        DLog.d("filedb get list type:" + type);

        String tableName = FileBean.getTypeName(type);
        if(tableName == null)
            return null;

        List<FileBean> fileList = new ArrayList<FileBean>();

        FileCursorWrapper cursor = queryFiles(tableName, null, null);
        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                fileList.add(cursor.getFile());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return fileList;
    }

    private static ContentValues getContentValues(FileBean fb){
        ContentValues values = new ContentValues();
        values.put(FileDBTable.Cols.UUID, fb.getUuid().toString());
        values.put(FileDBTable.Cols.TYPE, fb.getType());
        values.put(FileDBTable.Cols.SRCPATH, fb.getSrcPath());
        values.put(FileDBTable.Cols.DELTIME, fb.getDelTime().getTime());

        return values;
    }
}
