package com.trend.lazyinject.lib.reflection;

import android.os.Bundle;
import android.os.IBinder;

public class BundleMirro {
    public static RefMethod<Void> putIBinder = new RefMethod(Bundle.class, "putIBinder", String.class, IBinder.class);
    public static RefMethod<IBinder> getIBinder = new RefMethod(Bundle.class, "getIBinder", String.class);;
}
