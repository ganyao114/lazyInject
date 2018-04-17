package com.trend.lazyinject.lib.component;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.DebugLog;
import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.Name;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.lib.cache.ProviderCache;
import com.trend.lazyinject.lib.di.DIImpl;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global Name Manager
 * Created by ganyao on 2017/3/16.
 */

public class ComponentManager {

    private static final String TAG = "ComponentManager";
    public static Map<Object,Object> components = new ConcurrentHashMap<>();

    private static Map<String,Class> typeMap = new ConcurrentHashMap<>();

    private static Map<Class,ProviderCache> providerCache = new ConcurrentHashMap<>();
    private static Map<Field,Method> providerCacheTop = new ConcurrentHashMap<>();

    public static <T> void registerComponent(Class<T> type, T instance) {
        registerComponent(type, instance, true);
    }

    public static <T> void registerComponent(Class<T> type, T instance, boolean cacheProvider) {
        if (!type.isInstance(instance))
            return;
        Name name = (Name) type.getAnnotation(Name.class);
        if (name != null) {
            if (!TextUtils.isEmpty(name.value())) {
                registerComponent(type, instance, name.value());
                return;
            }
        }
        components.put(type, instance);
        if (cacheProvider) {
            ComponentBuilder.registerProviderAsync(type);
        }
    }

    public static <T> void registerComponent(Class type, T instance, String key) {
        if (!type.isInstance(instance))
            return;
        components.put(type, instance);
        typeMap.put(key, type);
    }

    @DebugLog
    public static <T> T getComponent(Class<T> type) {
        T t = (T) components.get(type);
        if (t != null)
            return t;
        synchronized (type) {
            t = (T) components.get(type);
            if (t == null) {
                for (Object inst:components.values()) {
                    if (type.isInstance(inst)) {
                        return (T) inst;
                    }
                }
                BuildWrapper<T> wrapper = ComponentBuilder.buildWrapper(type);
                if (wrapper != null) {
                    t = wrapper.component;
                    if (!wrapper.noCache) {
                        registerComponent(type, t, false);
                    }
                }
            }
            return t;
        }
    }

    public static <T> T getComponent(String key) {
        Class type = typeMap.get(key);
        if (type == null)
            return null;
        return (T) getComponent(type);
    }

    public static void removeComponent(Class type) {
        synchronized (type) {
            Object o = components.remove(type);
            if (o != null) {
                if (o instanceof IComponentDestroy) {
                    Destroyed destroyed = ((IComponentDestroy) o).onComponentDestroyed();
                    destroyed.isDestroyed = true;
                }
            }
        }
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

    private static void doInjectComponent(InjectComponent injectComponent, Object target, Field field) {
        Class componentType = field.getType();
        Object component = null;
        String name = injectComponent.value();
        if (TextUtils.isEmpty(name)) {
            component = getComponent(componentType);
        } else {
            component = getComponent(name);
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
        Object componentImpl = getComponentImpl(component, components);
        if (componentImpl == null)
            return;
        Object value = null;
        if (ValidateUtil.isEmpty(inject.args())) {
            value = providerValue(component, field, componentImpl);
        } else {
            value = providerValue(component, field, componentImpl, inject.args());
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

    @DebugLog
    public static Object providerValue(Class componentType, Field field, Object component) {
        Method provider = providerCacheTop.get(field);
        if (provider == null) {
            provider = doGetProvider(componentType, field.getGenericType());
            if (provider == null)
                return null;
            providerCacheTop.put(field, provider);
        }
        if (component == null) {
            component = getComponent(componentType);
        }
        if (component == null)
            return null;
        Class[] pars = provider.getParameterTypes();
        try {
            Object value = null;
            if (ValidateUtil.isEmpty(pars)) {
                value = provider.invoke(component);
            } else {
                value = provider.invoke(component, initObjects(new String[pars.length]));
            }
            return value;
        } catch (Exception e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + field.toString() + " get error!", e);
            return null;
        }
    }

    @DebugLog
    public static Object providerValue(Class componentType, Field field, Object component, Object... args) {
        Method provider = providerCacheTop.get(field);
        if (provider == null) {
            provider = doGetProvider(componentType, field.getGenericType());
            if (provider == null)
                return null;
            providerCacheTop.put(field, provider);
        }
        if (component == null) {
            component = getComponent(componentType);
        }
        if (component == null)
            return null;
        try {
            Object value = provider.invoke(component, args);
            return value;
        } catch (Exception e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + field.toString() + " get error!", e);
            return null;
        }
    }

    @DebugLog
    public static Object providerValue(Class componentType, Type fieldType) {
        Method provider = doGetProvider(componentType, fieldType);
        if (provider == null)
            return null;
        Object component = getComponent(componentType);
        if (component == null)
            return null;
        try {
            Object value = provider.invoke(component);
            return value;
        } catch (Exception e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + fieldType.toString() + " get error!", e);
            return null;
        }
    }

    @DebugLog
    public static Object providerValue(Class componentType, Type fieldType, Object... args) {
        Method provider = doGetProvider(componentType, fieldType);
        if (provider == null)
            return null;
        Object component = getComponent(componentType);
        if (component == null)
            return null;
        try {
            Object value = provider.invoke(component, args);
            return value;
        } catch (Exception e) {
            LOG.LOGE("ComponentManager", "Component providerValue :" + fieldType.toString() + " get error!", e);
            return null;
        }
    }

    private static Method doGetProvider(Class componentType, Type fieldType) {
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
        Method provider = cache.getProvider(fieldType);
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

    private static Object[] initObjects(Object... objects) {
        if (objects == null)
            return objects;
        for (int i = 0;i < objects.length;i ++) {
            objects[i] = "";
        }
        return objects;
    }

    private static Object getComponentImpl(Class component, Object[] componentImpls) {
        if (ValidateUtil.isEmpty(componentImpls))
            return null;
        for (Object impl:componentImpls) {
            if (component.isInstance(impl)) {
                return impl;
            }
        }
        return null;
    }


}
