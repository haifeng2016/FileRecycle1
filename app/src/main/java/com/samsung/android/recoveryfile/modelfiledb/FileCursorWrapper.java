package com.samsung.android.recoveryfile.modelfiledb;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.samsung.android.filerecycle.common.DLog;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.modelfiledb.FileDBSchema.FileDBTable;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by samsung on 2017/6/7.
 */

public class FileCursorWrapper extends CursorWrapper{
    public FileCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public FileBean getFile() {
        String uuidString = getString(getColumnIndex(FileDBTable.Cols.UUID));
        int type = getInt(getColumnIndex(FileDBTable.Cols.TYPE));
        String srcPath = getString(getColumnIndex(FileDBTable.Cols.SRCPATH));
        long delTime = getLong(getColumnIndex(FileDBTable.Cols.DELTIME));
        Date date = new Date(delTime);

        FileBean fb = new FileBean(UUID.fromString(uuidString), type, srcPath, date);
        File f = new File(fb.getBackupPath());
        fb.setSize(f.length());
        DLog.d("filedb generate type:" + type + " file:" + srcPath + " deltime:" + date.toString());
        return fb;
    }
}
