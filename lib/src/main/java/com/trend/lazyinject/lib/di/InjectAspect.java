package com.trend.lazyinject.lib.di;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.utils.ReflectUtils;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;


/**
 * Created by swift_gan on 2017/12/7.
 */
@Aspect
public class InjectAspect {



    @Pointcut("get(* *) && @annotation(inject)")
    public void pointcutInject(Inject inject) {

    }

    @Around("pointcutInject(inject)")
    public Object aroundFieldGet(ProceedingJoinPoint joinPoint, Inject inject) throws Throwable {
        Object targetObj = joinPoint.getTarget();
        Field field = ReflectUtils.getField(joinPoint, Inject.class);
        if (field == null)
            return joinPoint.proceed();
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object res = null;
        if (!inject.alwaysRefresh()) {
            if (field.get(targetObj) != null)
                return joinPoint.proceed();
            synchronized (getInjectLock(field, targetObj)) {
                if (field.get(targetObj) != null)
                    return joinPoint.proceed();
                res = getValue(inject.component(), field, inject.args());
                if (res == null) {
                    if (inject.nullProtect()) {
                        res = InterfaceProxy.make(field.getType());
                    }
                    return res;
                }
                field.set(targetObj, res);
            }
        } else {
            res = getValue(inject.component(), field, inject.args());
            if (res == null) {
                if (inject.nullProtect()) {
                    res = InterfaceProxy.make(field.getType());
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
