package com.trend.lazyinject.lib;

import com.trend.lazyinject.lib.component.ComponentBuilder;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.di.DIImpl;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.log.MethodMonitor;

/**
 * Created by ganyao on 2018/4/16.
 */

public class LazyInject {

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
