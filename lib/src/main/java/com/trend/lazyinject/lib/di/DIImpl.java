package com.trend.lazyinject.lib.di;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.DebugLog;
import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.annotation.Provide;
import com.trend.lazyinject.lib.cache.ProviderCache;
import com.trend.lazyinject.lib.component.ComponentBuilder;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.provider.DefaultProvider;
import com.trend.lazyinject.lib.provider.IProvider;
import com.trend.lazyinject.lib.provider.ProviderWithInjectPars;
import com.trend.lazyinject.lib.provider.StringArgsProvider;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by ganyao on 2017/12/4.
 */

public class DIImpl {

    private static Map<Class, ComponentContainer> dis = new ConcurrentHashMap<>();

    private static Map<Class, ProviderCache> providerCache = new ConcurrentHashMap<>();
    private static Map<Field, IProvider> providerCacheTop = new ConcurrentHashMap<>();

    public static ComponentContainer registerProvider(Class component) {
        ComponentContainer container = dis.get(component);
        if (container != null)
            return container;
        synchronized (component) {
            container = dis.get(component);
            if (container != null)
                return container;
            container = new ComponentContainer();
            container.setComponentType(component);
            for (Method method : component.getMethods()) {
                if (!method.isAnnotationPresent(Provide.class))
                    continue;
                Type retType = method.getGenericReturnType();
                if (retType == null || retType.equals(Void.TYPE) || filterBase(method))
                    continue;
                IProvider provider = null;
                Type[] pars = method.getGenericParameterTypes();
                Annotation[][] annotations = method.getParameterAnnotations();
                if (ValidateUtil.isEmpty(pars)) {
                    provider = new DefaultProvider(method);
                } else {
                    ProviderWithInjectPars.ProviderInfo[] providerInfos = new ProviderWithInjectPars.ProviderInfo[pars.length];
                    boolean isInject = false;
                    for (int i = 0; i < pars.length; i++) {
                        Type par = pars[i];
                        ProviderWithInjectPars.ProviderInfo info = null;
                        Annotation[] annos = annotations[i];
                        Inject inject = null;
                        InjectComponent injectComponent = null;
                        if (!ValidateUtil.isEmpty(annos)) {
                            for (Annotation anno : annos) {
                                if (anno != null && anno instanceof Inject) {
                                    inject = (Inject) anno;
                                } else if (anno != null && anno instanceof InjectComponent) {
                                    injectComponent = (InjectComponent) anno;
                                }
                            }
                        }
                        if (inject != null) {
                            isInject = true;
                            info = new ProviderWithInjectPars.ProviderInfo(par, inject);
                        } else if (injectComponent != null) {
                            isInject = true;
                            info = new ProviderWithInjectPars.ProviderInfo(par, injectComponent);
                        }else if (String.class.equals(par)) {
                            info = new ProviderWithInjectPars.ProviderInfo();
                        } else {
                            isInject = true;
                            info = new ProviderWithInjectPars.ProviderInfo(par, new ProviderWithInjectPars.FakeInject(component));
                        }
                        providerInfos[i] = info;
                    }
                    if (isInject) {
                        provider = new ProviderWithInjectPars(method, providerInfos);
                    } else {
                        provider = new StringArgsProvider(method, pars.length);
                    }
                }
                container.addProvider(retType, provider);
            }
            return container;
        }
    }

    private static boolean filterBase(Method method) {
        Class decClazz = method.getDeclaringClass();
        if (decClazz == null)
            return true;
        return decClazz.equals(Object.class);
    }

    public static ComponentContainer getProvider(Class component) {
        Class cType = ComponentBuilder.getRawType(component);
        if (cType == null)
            return null;
        ComponentContainer container = dis.get(cType);
        if (container == null) {
            container = registerProvider(cType);
        }
        return container;
    }


    @DebugLog
    public static Object providerValue(Class componentType, Field field, Object component, String... args) {
        IProvider provider = providerCacheTop.get(field);
        if (provider == null) {
            provider = doGetProvider(componentType, field.getGenericType());
            if (provider == null)
                return null;
            providerCacheTop.put(field, provider);
        }
        if (component == null) {
            component = ComponentManager.getComponent(componentType);
        }
        if (component == null)
            return null;
        try {
            Object value = provider.provide(component, args);
            return value;
        } catch (Throwable e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + field.toString() + " get error!", e);
            return null;
        }
    }

    @DebugLog
    public static Object providerValue(Class componentType, Type fieldType, String... args) {
        IProvider provider = doGetProvider(componentType, fieldType);
        if (provider == null)
            return null;
        Object component = ComponentManager.getComponent(componentType);
        if (component == null)
            return null;
        try {
            Object value = provider.provide(component, args);
            return value;
        } catch (Throwable e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + fieldType.toString() + " get error!", e);
            return null;
        }
    }

    private static IProvider doGetProvider(Class componentType, Type fieldType) {
        ProviderCache cache = providerCache.get(componentType);
        if (cache == null) {
            synchronized (componentType) {
                cache = providerCache.get(componentType);
                if (cache == null) {
                    cache = new ProviderCache();
                    providerCache.put(componentType, cache);
                }
            }
        }
        IProvider provider = cache.getProvider(fieldType);
        if (provider != null)
            return provider;
        ComponentContainer container = DIImpl.getProvider(componentType);
        if (container == null)
            return null;
        provider = container.getProvider(fieldType);
        if (provider != null) {
            cache.addProvider(fieldType, provider);
        }
        return provider;
    }

    @DebugLog
    public static void inject(Object target) {
        Class type = target.getClass();
        doInject(target, type);
    }

    @DebugLog
    public static void injectNest(Object target) {
        Class<?> template = target.getClass();
        while (template != null && template != Object.class) {
            // 过滤掉基类 因为基类是不包含注解的
            String clazzName = template.getName();
            if (clazzName.startsWith("java.") || clazzName.startsWith("javax.")
                    || clazzName.startsWith("android.")) {
                break;
            }
            doInject(target, template);
            template = template.getSuperclass();
        }
    }

    public static void inject(Object target, Object... components) {
        if (ValidateUtil.isEmpty(components))
            return;
        Class<?> template = target.getClass();
        while (template != null && template != Object.class) {
            // 过滤掉基类 因为基类是不包含注解的
            String clazzName = template.getName();
            if (clazzName.startsWith("java.") || clazzName.startsWith("javax.")
                    || clazzName.startsWith("android.")) {
                break;
            }
            doInject(target, template, components);
            template = template.getSuperclass();
        }
    }

    private static void doInject(Object target, Class type, Object... components) {
        for (Field field:type.getDeclaredFields()) {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject != null) {
                doInjectProvide(inject, target, field, components);
                continue;
            }
        }
    }

    private static void doInject(Object target, Class type) {
        for (Field field:type.getDeclaredFields()) {
            Inject inject = field.getAnnotation(Inject.class);
            if (inject != null) {
                doInjectProvide(inject, target, field);
                continue;
            }
            InjectComponent injectComponent = field.getAnnotation(InjectComponent.class);
            if (injectComponent != null) {
                doInjectComponent(injectComponent, target, field);
                continue;
            }
        }
    }

    private static void doInjectProvide(Inject inject, Object target, Field field) {
        Class component = inject.component();
        if (component == Inject.None.class) {
            component = field.getType().getEnclosingClass();
            if (component == null || component == Object.class)
                return;
        }
        Object value = null;
        if (ValidateUtil.isEmpty(inject.args())) {
            value = providerValue(component, field, null);
        } else {
            value = providerValue(component, field, null, inject.args());
        }
        if (value == null && inject.nullProtect()) {
            value = InterfaceProxy.make(field.getType());
        }
        if (value == null)
            return;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, value);
        } catch (IllegalAccessException e) {
            LOG.LOGE("ComponentManager", "inject value error! : " + field.toString(), e);
        }
    }

    private static void doInjectProvide(Inject inject, Object target, Field field, Object... components) {
        Class component = inject.component();
        if (component == Inject.None.class) {
            component = field.getType().getEnclosingClass();
            if (component == null || component == Object.class)
                return;
        }
        Object componentImpl = ComponentManager.getComponentImpl(component, components);
        if (componentImpl == null)
            return;
        Object value = null;
        if (ValidateUtil.isEmpty(inject.args())) {
            value = providerValue(component, field, componentImpl);
        } else {
            value = providerValue(component, field, componentImpl, inject.args());
        }
        if (value == null && inject.nullProtect()) {
            value = InterfaceProxy.make(field.getType());
        }
        if (value == null)
            return;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(target, value);
        } catch (IllegalAccessException e) {
            LOG.LOGE("ComponentManager", "inject value error! : " + field.toString(), e);
        }
    }

    private static void doInjectComponent(InjectComponent injectComponent, Object target, Field field) {
        Class componentType = field.getType();
        Object component = null;
        String name = injectComponent.value();
        if (TextUtils.isEmpty(name)) {
            component = ComponentManager.getComponent(componentType);
        } else {
            component = ComponentManager.getComponent(name);
        }
        if (component == null && injectComponent.nullProtect()) {
            component = InterfaceProxy.make(componentType);
        }
        if (component != null) {
            if (!field.isAccessible())
                field.setAccessible(true);
            try {
                field.set(target, component);
            } catch (IllegalAccessException e) {
                LOG.LOGE(TAG, "Inject component " + field.getName() + " error!", e);
            }
        }
    }

}
