package com.trend.lazyinject.lib;

import android.content.Context;
import android.util.Log;

import com.trend.lazyinject.annotation.FieldGetHook;
import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.annotation.InjectInfo;
import com.trend.lazyinject.lib.component.ComponentBuilder;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.di.DIImpl;
import com.trend.lazyinject.lib.di.InjectComponentWeave;
import com.trend.lazyinject.lib.di.InjectWeave;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.log.MethodMonitor;

import java.lang.reflect.Field;

/**
 * Created by ganyao on 2018/4/16.
 */
public class LazyInject {

    static Context context;

    public static void init(Context context) {
        LazyInject.context = context;
        FieldGetHook.setHookInject(new FieldGetHook.HookInter() {
            @Override
            public Object onInject(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, Inject inject) {
                Log.e("LazyInject", "isStatic:" + isStatic + " - receiver:" + receiver + " - receiverType:" + receiverType + " - field:"+ field.getName() + " - injectInfo:" + inject);
                try {
                    return InjectWeave.inject(isStatic, receiver, field, receiverType, filedType, inject);
                } catch (Throwable throwable) {
                    LOG.LOGE("LazyInject", "inject field <" + receiverType.getCanonicalName() + "." + field.getName() + "> error!", throwable);
                }
                return null;
            }

            @Override
            public Object onInjectComponent(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, InjectComponent injectComponent) {
                Log.e("LazyInject", "isStatic:" + isStatic + " - receiver:" + receiver + " - receiverType:" + receiverType + " - field:"+ field.getName() + " - injectInfo:" + injectComponent);
                try {
                    return InjectComponentWeave.inject(isStatic, receiver, field, receiverType, filedType, injectComponent);
                } catch (Throwable throwable) {
                    LOG.LOGE("LazyInject", "inject field <" + receiverType.getCanonicalName() + "." + field.getName() + "> error!", throwable);
                }
                return null;
            }
        });
    }

    public static Context context() {
        return context;
    }

    public static void addBuildMap(Class... maps) {
        for (Class map:maps) {
            ComponentBuilder.addBuildMap(map);
        }
    }

    public static void inject(Object target) {
        inject(target, true);
    }

    public static void inject(Object target, Object... components) {
        DIImpl.inject(target, components);
    }

    public static void inject(Object target, boolean nest) {
        if (nest) {
            DIImpl.injectNest(target);
        } else {
            DIImpl.inject(target);
        }
    }

    public static <T> void registerComponent(Class<T> type, T instance) {
        ComponentManager.registerComponent(type, instance);
    }

    public static <T> T getComponent(Class<T> type) {
        return ComponentManager.getComponent(type);
    }

    public static void removeComponent(Class type) {
        ComponentManager.removeComponent(type);
    }

    public static void setDebug(boolean debug) {
        LOG.DEBUGGING_ENABLED = debug;
        MethodMonitor.DEBUG = debug;
    }

}
