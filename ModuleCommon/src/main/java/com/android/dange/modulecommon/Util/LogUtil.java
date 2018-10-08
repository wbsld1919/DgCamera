package com.android.dange.modulecommon.Util;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {

    // level，low to high.
    public static final int LOG_LEVEL_VERBOSE = 1;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_WARN = 4;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_NOLOG = 6;

    private static String AppName = "";
    private static boolean PrintLine = true;
    private static int LogLevel = LOG_LEVEL_VERBOSE;

    public static void setAppName(String appName) {
        AppName = appName;
    }

    /**
     * whether print the line number.
     */
    public static void setPrintLine(boolean enable) {
        PrintLine = enable;
    }

    /**
     * set the print level.
     */
    public static void setLogLevel(int logLevel) {
        LogLevel = logLevel;
    }

    public static void v() {
        if(LogLevel <= LOG_LEVEL_VERBOSE) {
            String tag = generateTag();
            Log.v("LogUtil", tag);
        }
    }

    public static void v(String msg) {
        if(LogLevel <= LOG_LEVEL_VERBOSE) {
            String tag = generateTag();
            Log.v(tag, msg);
        }
    }

    public static void d(String msg) {
        if(LogLevel <= LOG_LEVEL_DEBUG) {
            String tag = generateTag();
            Log.d(tag, msg);
        }
    }

    public static void i(String msg) {
        if(LogLevel <= LOG_LEVEL_INFO) {
            String tag = generateTag();
            Log.i(tag, msg);
        }
    }

    public static void w(String msg){
        if(LogLevel <= LOG_LEVEL_WARN) {
            String tag = generateTag();
            Log.w(tag, msg);
        }
    }

    public static void e(String msg){
        if(LogLevel <= LOG_LEVEL_ERROR) {
            String tag = generateTag();
            Log.e(tag, msg);
        }
    }

    /**
     * generate tag.
     */
    private static String generateTag() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String tag = "%s.%s";
        if(!TextUtils.isEmpty(AppName)){
            tag = AppName + "__" + tag;
        }
        if(PrintLine){
            tag += "(Line:%d)";
            tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        }else{
            tag = String.format(tag, callerClazzName, caller.getMethodName());
        }
        return tag;
    }
}
