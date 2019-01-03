package com.trend.lazyinject.lib.utils;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.trend.lazyinject.lib.reflection.BundleMirro;

public class BundlerCompat {

    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= 18) {
            return bundle.getBinder(key);
        } else {
            return BundleMirro.getIBinder.call(bundle, key);
        }
    }

    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, value);
        } else {
            BundleMirro.putIBinder.call(bundle, key, value);
        }
    }

}
