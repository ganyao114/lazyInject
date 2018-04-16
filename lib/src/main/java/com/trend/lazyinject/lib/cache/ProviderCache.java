package com.trend.lazyinject.lib.cache;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganyao on 2017/12/5.
 */

public class ProviderCache {


    public Map<Type,Method> cache = new ConcurrentHashMap<>();

    public Method getProvider(Type type) {
        return cache.get(type);
    }

    public void addProvider(Type type, Method method) {
        cache.put(type, method);
    }

}
