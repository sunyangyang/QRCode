package com.sunyy.qrcode.mylibrary;

import android.util.Log;

/**
 * Created by sunyangyang on 2018/1/5.
 */

public class LogUtils {
    private static final boolean DEBUG = true;

    public static void v(String tag, String content) {
        if (DEBUG)
            Log.v(tag, content);
    }

    public static void d(String tag, String content) {
        if (DEBUG)
            Log.d(tag, content);
    }

    public static void i(String tag, String content) {
        if (DEBUG)
            Log.i(tag, content);
    }

    public static void w(String tag, String content) {
        if (DEBUG)
            Log.w(tag, content);
    }

    public static void e(String tag, String content) {
        Log.e(tag, content);
    }

    public static void v(String tag, String content, Throwable throwable) {
        if (DEBUG)
            Log.v(tag, content, throwable);
    }

    public static void d(String tag, String content, Throwable throwable) {
        if (DEBUG)
            Log.d(tag, content, throwable);
    }

    public static void i(String tag, String content, Throwable throwable) {
        if (DEBUG)
            Log.i(tag, content, throwable);
    }

    public static void w(String tag, String content, Throwable throwable) {
        if (DEBUG)
            Log.w(tag, content, throwable);
    }

    public static void e(String tag, String content, Throwable throwable) {
        Log.e(tag, content, throwable);
    }

    public static void w(String tag, Throwable throwable) {
        if (DEBUG)
            Log.w(tag, throwable);
    }

}
