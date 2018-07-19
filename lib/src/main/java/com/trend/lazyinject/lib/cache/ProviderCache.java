package com.trend.lazyinject.lib.cache;

import com.trend.lazyinject.lib.provider.IProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganyao on 2017/12/5.
 */

public class ProviderCache {


    public Map<Type,IProvider> cache = new ConcurrentHashMap<>();

    public IProvider getProvider(Type type) {
        return cache.get(type);
    }

    public void addProvider(Type type, IProvider provider) {
        cache.put(type, provider);
    }

}
