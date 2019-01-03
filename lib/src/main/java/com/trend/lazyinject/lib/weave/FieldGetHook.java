package com.trend.lazyinject.lib.weave;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;

import java.lang.reflect.Field;

public class FieldGetHook {

    private static volatile HookInter hookInject;

    public static void setHookInject(HookInter hookInject) {
        FieldGetHook.hookInject = hookInject;
    }

    public final static Object hookInject(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, Inject inject) {
        return hookInject.onInject(isStatic, receiver, receiverType, field, filedType, inject);
    }

    public final static Object hookInjectComponent(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, InjectComponent injectComponent) {
        return hookInject.onInjectComponent(isStatic, receiver, receiverType, field, filedType, injectComponent);
    }

    public interface HookInter {
        Object onInject(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, Inject injectInfo);
        Object onInjectComponent(boolean isStatic, Object receiver, Class receiverType, Field field, Class filedType, InjectComponent injectInfo);
    }


}
