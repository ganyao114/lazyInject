package com.trend.lazyinject.lib.component;

import com.trend.lazyinject.annotation.DebugLog;
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
    private Map<Type,Method> providers = new HashMap<>();


    public Class getComponentType() {
        return componentType;
    }

    public void setComponentType(Class componentType) {
        this.componentType = componentType;
    }

    public void addProvider(Type type, Method method) {
        providers.put(type, method);
    }

    @DebugLog
    public Method getProvider(Type type) {
        Method method = providers.get(type);
        if (method == null) {
            for (Type key : providers.keySet()) {
                if (ReflectUtils.canCast(type, key)) {
                    method = providers.get(key);
                    break;
                }
            }
        }
        return method;
    }

}
