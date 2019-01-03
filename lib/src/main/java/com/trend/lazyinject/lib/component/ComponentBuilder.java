package com.trend.lazyinject.lib.component;

import android.text.TextUtils;

import com.trend.lazyinject.annotation.ComponentImpl;
import com.trend.lazyinject.annotation.DebugLog;
import com.trend.lazyinject.annotation.NoCache;
import com.trend.lazyinject.lib.LazyInject;
import com.trend.lazyinject.lib.di.ComponentContainer;
import com.trend.lazyinject.lib.di.DIImpl;
import com.trend.lazyinject.lib.exception.ComponentBuildException;
import com.trend.lazyinject.lib.ipc.IPCInvokeHandler;
import com.trend.lazyinject.lib.ipc.InjectIPCClientManager;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.thread.ThreadPool;
import com.trend.lazyinject.lib.utils.ProcessUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganyao on 2017/8/10.
 */

public class ComponentBuilder {

    final static String TAG = "ComponentBuilder";

    private static Map<Class,Method> injectors = new ConcurrentHashMap<>();
    private static Map<Class,Method> builders = new ConcurrentHashMap<>();

    public static <T> Object generate(Object target) {
        Class type = target.getClass();
        Method builderMethod = injectors.get(type);
        if (builderMethod == null) {
            for (Class key: injectors.keySet()) {
                if (key.isInstance(target)) {
                    builderMethod = injectors.get(key);
                    break;
                }
            }
        }
        if (builderMethod == null) {
            return null;
        }
        T component = null;
        try {
            component = (T) builderMethod.invoke(null, target);
        } catch (Exception e) {
            LOG.LOGE(TAG, "generate component error! target = " + type.getName(), e);
            return null;
        }
        return component;
    }

    @DebugLog
    public static <T> T build(Class<T> componentType) {
        Method builderMethod = builders.get(getRawType(componentType));
        if (builderMethod == null)
            return null;
        T component = null;
        try {
            component = (T) builderMethod.invoke(null);
        } catch (Exception e) {
            LOG.LOGE(TAG, "build component " + componentType.getName() + " error!", e);
            return null;
        }
        return component;
    }

    @DebugLog
    public static <T> BuildWrapper<T> buildWrapper(Class<T> componentType) {
        Method builderMethod = builders.get(getRawType(componentType));
        if (builderMethod == null) {
            LOG.LOGE(TAG, "can not find component type: " + componentType.getName() + " ,did you register?");
            return null;
        }
        T component = null;
        try {
            component = (T) builderMethod.invoke(null);
        } catch (Exception e) {
            throw new ComponentBuildException("build component " + componentType.getName() + " error!", e);
        }
        if (component == null)
            return null;
        return new BuildWrapper(component, builderMethod.isAnnotationPresent(NoCache.class));
    }

    public static <T> T doBuild(Class<T> componentType, Class<? extends T> componentImpl) {
        ComponentImpl component = componentImpl.getAnnotation(ComponentImpl.class);
        if (component == null) {
            return newInstance(componentImpl);
        }
        if (TextUtils.isEmpty(component.process())) {
            return newInstance(componentImpl);
        } else {
            if (TextUtils.equals(ProcessUtils.getProcessName(LazyInject.context()), component.process())) {
                return newInstance(componentImpl);
            } else {
                ComponentContainer container = DIImpl.registerProvider(componentType);
                if (container != null) {
                    container.setNeedIPC(true);
                    InjectIPCClientManager.setIPCProcess(componentType, component.process());
                }
                if (!componentType.isInterface())
                    throw new ComponentBuildException(componentType.getName() + " - component must be a interface when invoke ipc!");
                return InterfaceProxy.make(componentType, new IPCInvokeHandler(componentType));
            }
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Class<? extends T> getRawType(Class<T> inter) {
        if (ComponentManager.components.containsKey(inter))
            return inter;
        if (builders.containsKey(inter))
            return inter;
        if (inter == Object.class)
            return inter;
        for (Class cType:builders.keySet()) {
            if (inter.isAssignableFrom(cType)) {
                return cType;
            }
        }
        return inter;
    }

    /**
     * for dagger
     * @param target
     * @param args
     * @param <T>
     * @return
     */
    @DebugLog
    public static <T> Object inject(Object target, Object... args) {
        Class type = target.getClass();
        Method builderMethod = injectors.get(type);
        Class parType = null;
        if (builderMethod == null) {
            for (Class key: injectors.keySet()) {
                if (key.isInstance(target)) {
                    builderMethod = injectors.get(key);
                    parType = key;
                    break;
                }
            }
        } else {
            parType = type;
        }
        if (builderMethod == null) {
            return null;
        }
        T component = null;
        Object[] par = null;
        if (args == null || args.length == 0) {
            par = new Object[] {target};
        } else {
            par = new Object[args.length + 1];
            par[0] = target;
            for (int i = 0;i < args.length;i ++) {
                par[i + 1] = args[i];
            }
        }
        try {
            component = (T) builderMethod.invoke(null, par);
        } catch (Exception e) {
            LOG.LOGE(TAG, "build component " + builderMethod.getName() + " error!", e);
            return null;
        }
        if (component == null)
            return null;
        Method inject = null;
        try {
            inject = component.getClass().getDeclaredMethod("inject", parType);
        } catch (NoSuchMethodException e) {
            LOG.LOGV(TAG, "lazy search fail");
        }
        if (inject == null) {
            for (Method method:component.getClass().getDeclaredMethods()) {
                Class[] pars = method.getParameterTypes();
                if (pars == null || pars.length != 1)
                    continue;
                if (pars[0].isInstance(target)) {
                    inject = method;
                    break;
                }
            }
        }
        if (inject != null) {
            try {
                inject.invoke(component, target);
            } catch (Exception e) {
                LOG.LOGE(TAG, "inject error!", e);
            }
        }
        return component;
    }

    /**
     * for dagger
     * @param component
     * @param target
     */
    @DebugLog
    public static void injectOnly(Object component, Object target) {
        Method inject = null;
        Class parType = target.getClass();
        try {
            inject = component.getClass().getDeclaredMethod("inject", parType);
        } catch (NoSuchMethodException e) {
            LOG.LOGV(TAG, "lazy search fail");
        }
        if (inject == null) {
            for (Method method:component.getClass().getDeclaredMethods()) {
                Class[] pars = method.getParameterTypes();
                Class ret = method.getReturnType();
                if (pars == null || pars.length != 1)
                    continue;
                if (pars[0].isInstance(target)) {
                    inject = method;
                    break;
                }
            }
        }
        if (inject != null) {
            try {
                inject.invoke(component, target);
            } catch (Exception e) {
                LOG.LOGE(TAG, "inject error!", e);
            }
        }
    }

    public static void addBuildMap(Class map) {
        for (Method method:map.getDeclaredMethods()) {
            Class[] parTypes = method.getParameterTypes();
            if (!Modifier.isStatic(method.getModifiers()))
                continue;
            if (parTypes.length >= 1) {
                injectors.put(parTypes[0], method);
            } else if (parTypes.length == 0) {
                Class componentType = method.getReturnType();
                if (componentType != null) {
                    builders.put(componentType, method);
                    registerProviderAsync(componentType);
                }
            }
        }
    }

    public static void registerProviderAsync(final Class component) {
        ThreadPool.DEFAULT.submit(new Runnable() {
            @Override
            public void run() {
                DIImpl.registerProvider(component);
            }
        });
    }

}
