package com.trend.lazyinject.lib.component;

import com.trend.lazyinject.lib.provider.IProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganyao on 2018/7/21.
 */

public class Singletons {

    private Map<IProvider,Object> singletons = new ConcurrentHashMap<>();

    public void addSingleton(IProvider provider, Object o) {
        singletons.put(provider, o);
    }

    public Object getSingleton(IProvider provider) {
        return singletons.get(provider);
    }
}
