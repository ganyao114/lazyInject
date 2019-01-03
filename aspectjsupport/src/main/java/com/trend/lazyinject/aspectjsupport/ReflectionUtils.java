package com.trend.lazyinject.aspectjsupport;

import com.trend.lazyinject.lib.utils.ReflectUtils;

import org.aspectj.lang.JoinPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ReflectionUtils {
    public final static Field getField(JoinPoint joinPoint, Class<? extends Annotation> annoType) {
        Object instance = joinPoint.getTarget();
        Class clazz = joinPoint.getSourceLocation().getWithinType();
        String name = joinPoint.getSignature().getName();
        Field field = null;
        if (clazz != null && clazz != ClassNotFoundException.class) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
            if (field == null || !field.isAnnotationPresent(annoType)) {
                field = ReflectUtils.getField(clazz, name, annoType);
            }
        }
        if (field == null && instance != null) {
            field = ReflectUtils.getField(instance.getClass(), name, annoType);
        }
        return field;
    }
}
