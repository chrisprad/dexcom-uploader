package com.nightscout.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nightscout.app.upload.Uploader;

/*
 * Used as a quick logging framework facade when converting from Android to Java. 
 * The slf4j facade should now be used.
 */
@Deprecated
public final class AndroidLogProxy {
    public static final int ASSERT = 7;
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;
    
    static final Logger Log = LoggerFactory.getLogger(Uploader.class);
    
    public AndroidLogProxy() {
        
    }

    public static int v(String tag, String msg) {
        Log.debug(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int v(String tag, String msg, Throwable tr) {
    	Log.debug(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static int d(String tag, String msg) {
    	Log.debug(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int d(String tag, String msg, Throwable tr) {
    	Log.debug(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static int i(String tag, String msg) {
    	Log.info(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int i(String tag, String msg, Throwable tr) {
    	Log.info(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static int w(String tag, String msg) {
    	Log.warn(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int w(String tag, String msg, Throwable tr) {
    	Log.warn(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static native boolean isLoggable(String var0, int var1);

    public static int w(String tag, Throwable tr) {
    	Log.warn(String.format("%s: %s", tag, tr.getMessage()));
        return 1;
    }

    public static int e(String tag, String msg) {
    	Log.error(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int e(String tag, String msg, Throwable tr) {
    	Log.error(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static int wtf(String tag, String msg) {
    	Log.error(String.format("%s: %s", tag, msg));
        return 1;
    }

    public static int wtf(String tag, Throwable tr) {
    	Log.error(String.format("%s: %s", tag, tr.getMessage()));
        return 1;
    }

    public static int wtf(String tag, String msg, Throwable tr) {
    	Log.error(String.format("%s: %s\r\n%s", tag, msg,tr.getMessage()));
        return 1;
    }

    public static String getStackTraceString(Throwable tr) {
    	return tr.getMessage();
    }

    public static int println(int priority, String tag, String msg) {
    	Log.info(String.format("%s: %s", tag, msg));
        return 1;
    }
}
