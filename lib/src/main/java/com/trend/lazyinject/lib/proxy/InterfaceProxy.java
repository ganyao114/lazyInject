package com.trend.lazyinject.lib.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by swift_gan on 2018/4/27.
 */

public class InterfaceProxy {

    static Map<Class,Object> proxies = new ConcurrentHashMap<>();

    public static <T> T make(Class<T> inter) {
        return make(inter, new EmptyHandler());
    }

    public static <T> T make(Class<T> inter, InvocationHandler handler) {
        if (inter == null)
            return null;
        T proxy = (T) proxies.get(inter);
        if (proxy != null)
            return proxy;
        if (inter.isInterface() || Modifier.isAbstract(inter.getModifiers())) {
            try {
                proxy = (T) Proxy.newProxyInstance(inter.getClassLoader(),
                        new Class[]{inter},
                        handler);
            } catch (Exception e) {
            }
        }
        if (proxy != null) {
            proxies.put(inter, proxy);
        }
        return proxy;
    }

}
