package com.zql.android.buz;


import android.util.Log;

public class Zog {

    private static int count= 0;
    public static String getExternalInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(count).append("[").append(Thread.currentThread().getId()).append("-").append(Thread.currentThread().getName())
                .append("]");
        sb.append(":");
        count ++ ;
        return sb.toString();
    }

    public static void tagd(String msg) {
        d(Buz.TAG, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, getExternalInfo() + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, getExternalInfo() + msg);
    }

    public static void e(String tag, String msg, Exception e) {
        Log.e(tag, getExternalInfo() + msg, e);
    }
}
