package rtmppusher.demo.com.rtmppusher.utils.log;

import android.util.Log;

import rtmppusher.demo.com.rtmppusher.BuildConfig;

/**
 * Created by sym on 3/15/16.
 */
public class NSLog {

    public static void i(Object o, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(o.getClass().getSimpleName(), msg);
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, Object obj) {
        if (BuildConfig.DEBUG && obj != null) {
            Log.i(tag, obj.toString());
        }
    }

    public static void d(Object o, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(o.getClass().getSimpleName(), msg);
        }
    }

    public static void d(String tag, String msg) {

        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, Object obj) {
        if (BuildConfig.DEBUG && obj != null) {
            Log.d(tag, obj.toString());
        }
    }

    public static void w(Object o, String msg) {

        if (BuildConfig.DEBUG) {
            Log.w(o.getClass().getSimpleName(), msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, Object obj) {
        if (BuildConfig.DEBUG && obj != null) {
            Log.i(tag, obj.toString());
        }
    }

    public static void e(Object o, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(o.getClass().getSimpleName(), msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, Object obj) {
        if (BuildConfig.DEBUG && obj != null) {
            Log.e(tag, obj.toString());
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, tr);
        }
    }

}
