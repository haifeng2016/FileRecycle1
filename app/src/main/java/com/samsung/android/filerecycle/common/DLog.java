package com.samsung.android.filerecycle.common;

import android.util.Log;

import com.samsung.android.filerecycle.BuildConfig;

/**
 * Created by samsung on 2017/5/12.
 */

public class DLog {

    private static final String LOG_TAG = "liangyi";

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static boolean isLogDetail() {
        return BuildConfig.LOG_DETAIL;
    }

    private static String createLog(String log, StackTraceElement[] sElements){
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(sElements[1].getFileName());
        buffer.append(":");
        buffer.append(sElements[1].getMethodName());
        buffer.append(":");
        buffer.append(sElements[1].getLineNumber());
        buffer.append("]");
        buffer.append(log);
        return buffer.toString();
    }

    public static void i(String message) {
        if(!isDebuggable())
            return;

        if(isLogDetail()){
            Log.i(LOG_TAG, createLog(message, new Throwable().getStackTrace()));
        }else{
            Log.i(LOG_TAG, message);
        }
    }

    public static void d(String message) {
        if(!isDebuggable())
            return;

        if(isLogDetail()){
            Log.d(LOG_TAG, createLog(message, new Throwable().getStackTrace()));
        }else{
            Log.d(LOG_TAG, message);
        }
    }

    public static void v(String message) {
        if(!isDebuggable())
            return;

        if(isLogDetail()){
            Log.v(LOG_TAG, createLog(message, new Throwable().getStackTrace()));
        }else{
            Log.v(LOG_TAG, message);
        }
    }

    public static void w(String message) {
        if(!isDebuggable())
            return;

        if(isLogDetail()){
            Log.w(LOG_TAG, createLog(message, new Throwable().getStackTrace()));
        }else{
            Log.w(LOG_TAG, message);
        }
    }

    public static void e(String message) {
        if(isLogDetail()){
            Log.e(LOG_TAG, createLog(message, new Throwable().getStackTrace()));
        }else{
            Log.e(LOG_TAG, message);
        }
    }
}
