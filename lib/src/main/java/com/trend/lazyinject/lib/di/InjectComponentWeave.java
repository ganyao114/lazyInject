package com.trend.lazyinject.lib.di;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;

import java.lang.reflect.Field;

/**
 * Created by ganyao on 2017/12/5.
 */
public class InjectComponentWeave {

    public static Object inject(boolean isStatic, Object target, Field field, Class targetClass, Class fieldType,InjectComponent injectComponent) throws Throwable {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object component = field.get(target);
        if (component != null && !injectComponent.alwaysRefresh())
            return component;
        if (injectComponent.alwaysRefresh()) {
            String name = injectComponent.value();
            if (TextUtils.isEmpty(name)) {
                component = ComponentManager.getComponent(fieldType);
            } else {
                component = ComponentManager.getComponent(name);
            }
            if (component == null && injectComponent.nullProtect()) {
                component = InterfaceProxy.make(field.getType());
            }
            return component;
        } else {
            synchronized (getInjectLock(field, target)) {
                component = field.get(target);
                if (component != null)
                    return component;
                String name = injectComponent.value();
                if (TextUtils.isEmpty(name)) {
                    component = ComponentManager.getComponent(field.getType());
                } else {
                    component = ComponentManager.getComponent(name);
                }
                if (component != null) {
                    try {
                        field.set(target, component);
                        return component;
                    } catch (IllegalAccessException e) {
                        LOG.LOGE("InjectComponentAspect", "Inject component " + field.getName() + " error!", e);
                    }
                }
                if (component == null && injectComponent.nullProtect()) {
                    component = InterfaceProxy.make(field.getType());
                }
                return component;
            }
        }
    }

    private static Object getInjectLock(Field field, Object target) {
        return ("LZ_DI_LOCK:" + field.hashCode() + "@" + (target != null ? System.identityHashCode(target) : "")).intern();
    }

}
