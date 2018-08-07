package com.trend.lazyinject.lib.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RefMethod<T> {

    Method method;

    public RefMethod(Class clazz, String name, Class... args) {
        try {
            method = clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public T call(Object receiver, Object... args) {
        if (method == null)
            return null;
        try {
            return (T) this.method.invoke(receiver, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
