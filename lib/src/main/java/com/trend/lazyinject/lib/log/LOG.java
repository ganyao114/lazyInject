package com.trend.lazyinject.lib.log;

import android.text.TextUtils;
import android.util.Log;

import com.trend.lazyinject.BuildConfig;


public class LOG {

    public static boolean DEBUGGING_ENABLED = BuildConfig.DEBUG;

    public static void LOGV(String tag, String message) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.v(tag, message);
        }
    }

    public static void LOGD(String tag, String message) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.d(tag, message);
        }
    }

    public static void LOGI(String tag, String message) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.i(tag, message);
        }
    }

    public static void LOGW(String tag, String message) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.w(tag, message);
        }
    }

    public static void LOGE(String tag, String message) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.e(tag, message);
        }
    }

    public static void LOGE(String tag, String message, Throwable throwable) {
        if (DEBUGGING_ENABLED && !TextUtils.isEmpty(message)) {
            Log.e(tag, message, throwable);
        }
    }
}
