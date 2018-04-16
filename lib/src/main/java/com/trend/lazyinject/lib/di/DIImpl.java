package com.trend.lazyinject.lib.di;

import com.trend.lazyinject.annotation.Exclude;
import com.trend.lazyinject.lib.component.ComponentBuilder;
import com.trend.lazyinject.lib.component.ComponentContainer;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganyao on 2017/12/4.
 */

public class DIImpl {

    private static Map<Class,ComponentContainer> dis = new ConcurrentHashMap<>();

    public static ComponentContainer registerProvider(Class component) {
        ComponentContainer container = dis.get(component);
        if (container != null)
            return container;
        synchronized (component) {
            container = dis.get(component);
            if (container != null)
                return container;
            container = new ComponentContainer();
            container.setComponentType(component);
methods:    for (Method method:component.getMethods()) {
                Type retType = method.getGenericReturnType();
                if (retType == null || retType.equals(Void.TYPE) || filterBase(method))
                    continue;
                if (method.isAnnotationPresent(Exclude.class))
                    continue;
                Class[] pars = method.getParameterTypes();
                if (!ValidateUtil.isEmpty(pars)) {
                    for (Class par:pars) {
                        if (!String.class.equals(par) && !String[].class.equals(par)) {
                            continue methods;
                        }
                    }
                }
                container.addProvider(retType, method);
            }
            return container;
        }
    }

    private static boolean filterBase(Method method) {
        Class decClazz = method.getDeclaringClass();
        if (decClazz == null)
            return true;
        return decClazz.equals(Object.class);
    }

    public static ComponentContainer getProvider(Class component) {
        Class cType = ComponentBuilder.getRawType(component);
        if (cType == null)
            return null;
        ComponentContainer container = dis.get(cType);
        if (container == null) {
            container = registerProvider(cType);
        }
        return container;
    }

}
