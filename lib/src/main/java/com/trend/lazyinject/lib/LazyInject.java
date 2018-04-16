package com.trend.lazyinject.lib;

import com.trend.lazyinject.lib.component.ComponentBuilder;
import com.trend.lazyinject.lib.component.ComponentManager;

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

    }

    public static void inject(Object target, boolean nest) {
        if (nest) {
            ComponentManager.injectNest(target);
        } else {
            ComponentManager.inject(target);
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

}
