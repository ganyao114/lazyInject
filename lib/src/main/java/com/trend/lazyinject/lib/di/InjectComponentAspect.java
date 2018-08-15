package com.trend.lazyinject.lib.di;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.utils.ReflectUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;

/**
 * Created by ganyao on 2017/12/5.
 */
@Aspect
public class InjectComponentAspect {

    @Pointcut("get(* *) && @annotation(injectComponent)")
    public void pointcutInjectComponent(InjectComponent injectComponent) {

    }

    @Around("pointcutInjectComponent(injectComponent)")
    public Object aroundFieldGet(ProceedingJoinPoint joinPoint, InjectComponent injectComponent) throws Throwable {
        Object targetObj = joinPoint.getTarget();
        Field field = ReflectUtils.getField(joinPoint, InjectComponent.class);
        if (field == null)
            return joinPoint.proceed();
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object component = field.get(targetObj);
        if (component != null && !injectComponent.alwaysRefresh())
            return joinPoint.proceed();
        if (injectComponent.alwaysRefresh()) {
            String name = injectComponent.value();
            if (TextUtils.isEmpty(name)) {
                component = ComponentManager.getComponent(field.getType());
            } else {
                component = ComponentManager.getComponent(name);
            }
            if (component == null && injectComponent.nullProtect()) {
                component = InterfaceProxy.make(field.getType());
            }
            return component;
        } else {
            synchronized (getInjectLock(field, targetObj)) {
                component = field.get(targetObj);
                if (component != null)
                    return joinPoint.proceed();
                String name = injectComponent.value();
                if (TextUtils.isEmpty(name)) {
                    component = ComponentManager.getComponent(field.getType());
                } else {
                    component = ComponentManager.getComponent(name);
                }
                if (component != null) {
                    try {
                        field.set(targetObj, component);
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
