package com.samsung.android.recoveryfile.mainpresenter;

import java.util.Date;
import java.util.UUID;

/**
 * Created by samsung on 2017/5/17.
 */

public class FileBean{
    public FileBean(UUID id, int t, String path, Date time){
        uuid = id;
        srcPath = path;
        type = t;
        delTime = time;
        name = path.substring(path.lastIndexOf("/")+1);
        setSubType();
    }

    public FileBean(int t, String path, Date time){
        this(UUID.randomUUID(), t, path, time);
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getType() {
        return type;
    }

    public int getSubtype() {
        return subtype;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public String getBackupPath() {
        return MainPresenterService.getFileBackupDir() + getTypeName(type) + "/" + uuid.toString();
    }

    public String getThumbnailPath() {
        return MainPresenterService.getFileBackupDir() + "thumbnails" + "/" + uuid.toString();
    }

    public Date getDelTime() {
        return delTime;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public static String getTypeName(int type){
        switch(type){
            case TYPE.IMG:
                return TYPE_NAME.IMG;
            case TYPE.MUSIC:
                return TYPE_NAME.MUSIC;
            case TYPE.VIDEO:
                return TYPE_NAME.VIDEO;
            case TYPE.DOC:
                return TYPE_NAME.DOC;
            case TYPE.APP:
                return TYPE_NAME.APP;
            case TYPE.OTHER:
                return TYPE_NAME.OTHER;
            default:
                return null;
        }
    }


    public static int fileTypeFilter(String sname){
        if(sname == null)
            return -1;

        String prefix = sname.substring(sname.lastIndexOf(".") + 1);
        if(prefix.equals("jpg") || prefix.equals("png") || prefix.equals("tif")){
            return FileBean.TYPE.IMG;
        }else if(prefix.equals("mp3") || prefix.equals("wma") || prefix.equals("ogg")){
            return FileBean.TYPE.MUSIC;
        }else if(prefix.equals("mp4") || prefix.equals("avi")){
            return FileBean.TYPE.VIDEO;
        }else if(prefix.equals("pdf") || prefix.equals("xls") || prefix.equals("xlsx") || prefix.equals("ppt") || prefix.equals("pptx")
                || prefix.equals("doc") || prefix.equals("docx")  || prefix.equals("txt")){
            return FileBean.TYPE.DOC;
        }else if(prefix.equals("zip") || prefix.equals("rar")){
            return FileBean.TYPE.OTHER;
        }

        return -1;
    }

    private void setSubType(){
        String prefix = srcPath.substring(srcPath.lastIndexOf(".") + 1);
        if(prefix.equals("jpg")){subtype = SUBTYPE.JPG;
        }else if(prefix.equals("png")){subtype = SUBTYPE.PNG;
        }else if(prefix.equals("tif")){subtype = SUBTYPE.TIF;
        }else if(prefix.equals("mp3")){subtype = SUBTYPE.MP3;
        }else if(prefix.equals("wma")){subtype = SUBTYPE.WMA;
        }else if(prefix.equals("ogg")){subtype = SUBTYPE.OGG;
        }else if(prefix.equals("mp4")){subtype = SUBTYPE.MP4;
        }else if(prefix.equals("avi")){subtype = SUBTYPE.AVI;
        }else if(prefix.equals("pdf")){subtype = SUBTYPE.PDF;
        }else if(prefix.equals("xls")){subtype = SUBTYPE.XLS;
        }else if(prefix.equals("xlsx")){subtype = SUBTYPE.XLS;
        }else if(prefix.equals("ppt")){subtype = SUBTYPE.PPT;
        }else if(prefix.equals("pptx")){subtype = SUBTYPE.PPT;
        }else if(prefix.equals("doc")){subtype = SUBTYPE.DOC;
        }else if(prefix.equals("docx")){subtype = SUBTYPE.DOC;
        }else if(prefix.equals("txt")){subtype = SUBTYPE.TXT;
        }else if(prefix.equals("zip")){subtype = SUBTYPE.ZIP;
        }else if(prefix.equals("rar")){subtype = SUBTYPE.RAR;
        }
    }

    public String typeName() {
        return getTypeName(type);
    }

    private UUID uuid;
    private String srcPath;
    private String name;
    private int type;
    private int subtype;
    private long size;
    private Date delTime;

    public static final class TYPE {
        public static final int IMG       = 0x0000;
        public static final int MUSIC     = 0x0001;
        public static final int VIDEO     = 0x0002;
        public static final int DOC       = 0x0003;
        public static final int OTHER     = 0x0004;
        public static final int APP       = 0x0005;
        public static final int TOTAL     = 0x0006;
    }

    public static final class SUBTYPE {
        public static final int JPG       = 0x0000;
        public static final int PNG       = 0x0001;
        public static final int TIF       = 0x0002;
        public static final int MP3       = 0x0003;
        public static final int WMA       = 0x0004;
        public static final int OGG       = 0x0005;
        public static final int MP4       = 0x0006;
        public static final int AVI       = 0x0007;
        public static final int PDF       = 0x0008;
        public static final int XLS       = 0x0009;
        public static final int PPT       = 0x000a;
        public static final int DOC       = 0x000b;
        public static final int TXT       = 0x000c;
        public static final int ZIP       = 0x000d;
        public static final int RAR       = 0x000e;
    }

    public static final class TYPE_NAME {
        public static final String IMG    = "image";
        public static final String MUSIC  = "music";
        public static final String VIDEO  = "video";
        public static final String DOC    = "document";
        public static final String APP    = "application";
        public static final String OTHER  = "other";
    }
}
