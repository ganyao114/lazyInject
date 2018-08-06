package com.trend.lazyinject.lib.proxy;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by swift_gan on 2018/4/27.
 */

public class EmptyHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Log.v("NotNull Protect", "method - " + method.getName() + " invoke in mock instance!");
        return null;
    }
}
