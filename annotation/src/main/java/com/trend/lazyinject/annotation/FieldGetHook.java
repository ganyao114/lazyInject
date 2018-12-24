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

    public static Object hookInject(Object receiver, Class receiverType, Object currentValue, String fieldName, Class filedType) {
        if (hookInject != null) {
            return hookInject.onFieldGet(receiver, receiverType, currentValue, fieldName, filedType);
        } else {
            return null;
        }
    }

    public static Object hookInjectComponent(Object receiver, Class receiverType, Object currentValue, String fieldName, Class filedType) {
        if (hookInjectComponent != null) {
            return hookInjectComponent.onFieldGet(receiver, receiverType, currentValue, fieldName, filedType);
        } else {
            return null;
        }
    }

    public interface HookInter {
        Object onFieldGet(Object receiver, Class receiverType, Object currentValue, String fieldName, Class filedType);
    }
}
