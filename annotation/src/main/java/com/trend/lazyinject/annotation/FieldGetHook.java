package com.trend.lazyinject.annotation;

public class FieldGetHook {

    private static volatile HookInter hookInject;
    private static volatile HookInter hookInjectComponent;

    public static void setHookInject(HookInter hookInject) {
        FieldGetHook.hookInject = hookInject;
    }

    public static void setHookInjectComponent(HookInter hookInjectComponent) {
        FieldGetHook.hookInjectComponent = hookInjectComponent;
    }

    public static Object hookInject(boolean isStatic, Object receiver, Class receiverType, String fieldName, Class filedType, InjectInfo injectInfo) {
        if (hookInject != null) {
            return hookInject.onFieldGet(isStatic, receiver, receiverType, fieldName, filedType, injectInfo);
        } else {
            return null;
        }
    }

    public static Object hookInjectComponent(boolean isStatic, Object receiver, Class receiverType, String fieldName, Class filedType, InjectInfo injectInfo) {
        if (hookInjectComponent != null) {
            return hookInjectComponent.onFieldGet(isStatic, receiver, receiverType, fieldName, filedType, injectInfo);
        } else {
            return null;
        }
    }

    public interface HookInter {
        Object onFieldGet(boolean isStatic, Object receiver, Class receiverType, String fieldName, Class filedType, InjectInfo injectInfo);
    }
}
