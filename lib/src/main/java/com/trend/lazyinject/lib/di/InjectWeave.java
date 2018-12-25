package com.trend.lazyinject.lib.di;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.reflect.Field;


/**
 * Created by swift_gan on 2017/12/7.
 */
public class InjectWeave {

    public static Object inject(boolean isStatic, Object target, Field field, Class targetClass, Class fieldType, Inject inject) throws Throwable {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object res = null;
        if (!inject.alwaysRefresh()) {
            res = field.get(target);
            if (res != null)
                return res;
            synchronized (getInjectLock(field, target)) {
                res = field.get(target);
                if (res != null)
                    return res;
                res = getValue(inject.component(), field, inject.args());
                if (res == null) {
                    if (inject.nullProtect()) {
                        res = InterfaceProxy.make(fieldType);
                    }
                    return res;
                }
                field.set(target, res);
            }
        } else {
            res = getValue(inject.component(), field, inject.args());
            if (res == null) {
                if (inject.nullProtect()) {
                    res = InterfaceProxy.make(fieldType);
                }
                return res;
            }
        }
        return res;
    }

    private static Object getValue(Class type, Field field, String[] args) {
        Object res = null;
        Class component = getComponentType(type, field);
        if (component == null)
            return null;
        if (ValidateUtil.isEmpty(args))
            res = DIImpl.providerValue(component, field, null);
        else
            res = DIImpl.providerValue(component, field, null, args);
        return res;
    }

    private static Class getComponentType(Class type, Field field) {
        Class component = type;
        if (component == Inject.None.class) {
            component = field.getType().getEnclosingClass();
            if (component == null || component == Object.class)
                return null;
        }
        return component;
    }

    private static Object getInjectLock(Field field, Object target) {
        return ("LZ_DI_LOCK:" + field.hashCode() + "@" + (target != null ? System.identityHashCode(target) : "")).intern();
    }

}
