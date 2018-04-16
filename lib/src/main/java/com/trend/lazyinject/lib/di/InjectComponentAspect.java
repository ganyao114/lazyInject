package com.trend.lazyinject.lib.di;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.log.LOG;
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

    @Pointcut("get(* *) && @annotation(injectComponent) && target(targetObj)")
    public void pointcutInjectComponent(InjectComponent injectComponent, Object targetObj) {

    }

    @Around("pointcutInjectComponent(injectComponent, targetObj)")
    public Object aroundFieldGet(ProceedingJoinPoint joinPoint, InjectComponent injectComponent, Object targetObj) throws Throwable {
        Field field = ReflectUtils.getField(targetObj.getClass(), joinPoint.getSignature().getName(), InjectComponent.class);
        if (field == null)
            return joinPoint.proceed();
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object component = field.get(targetObj);
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
        return joinPoint.proceed();
    }
}
