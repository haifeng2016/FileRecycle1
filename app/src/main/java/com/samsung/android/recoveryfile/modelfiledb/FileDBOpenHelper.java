package com.samsung.android.recoveryfile.modelfiledb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.modelfiledb.FileDBSchema.FileDBTable;
import com.samsung.android.filerecycle.common.DLog;

/**
 * Created by samsung on 2017/5/10.
 */

public class FileDBOpenHelper extends SQLiteOpenHelper{

    public static final String DB_NAME = "filerecycle.db";
    public static final int VERSION = 1;

    public FileDBOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DLog.d("db create");
        for(int i = 0; i < FileBean.TYPE.TOTAL; i++) {
            db.execSQL("create table " + FileBean.getTypeName(i) + "(" + " _id integer primary key autoincrement, " +
                    FileDBTable.Cols.UUID + ", " +
                    FileDBTable.Cols.TYPE + ", " +
                    FileDBTable.Cols.SRCPATH + ", " +
                    FileDBTable.Cols.DELTIME + ")"
            );
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
