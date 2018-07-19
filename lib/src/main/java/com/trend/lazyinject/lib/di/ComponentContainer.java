package com.trend.lazyinject.lib.di;

import com.trend.lazyinject.annotation.DebugLog;
import com.trend.lazyinject.lib.provider.IProvider;
import com.trend.lazyinject.lib.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganyao on 2017/12/4.
 */

public class ComponentContainer {

    private Class componentType;
    private Map<Type,IProvider> providers = new HashMap<>();


    public Class getComponentType() {
        return componentType;
    }

    public void setComponentType(Class componentType) {
        this.componentType = componentType;
    }

    public void addProvider(Type type, IProvider provider) {
        providers.put(type, provider);
    }

    @DebugLog
    public IProvider getProvider(Type type) {
        IProvider provider = providers.get(type);
        if (provider == null) {
            for (Type key : providers.keySet()) {
                if (ReflectUtils.canCast(type, key)) {
                    provider = providers.get(key);
                    break;
                }
            }
        }
        return provider;
    }

}
