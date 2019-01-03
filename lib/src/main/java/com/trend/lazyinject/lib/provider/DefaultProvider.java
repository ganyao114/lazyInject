package com.trend.lazyinject.lib.provider;

import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.component.Singletons;
import com.trend.lazyinject.lib.ipc.InjectIPCClientManager;
import com.trend.lazyinject.lib.ipc.LazyInjectIPC;
import com.trend.lazyinject.lib.utils.ValidateUtil;
import com.trend.lazyinject.lib.utils.ValueUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

public class DefaultProvider implements IProvider {

    protected Method providerMethod;
    protected Class componentType;
    private boolean singleton;

    volatile boolean needIPC = false;

    public DefaultProvider(Class componentType, Method providerMethod) {
        this.componentType = componentType;
        this.providerMethod = providerMethod;
    }

    @Override
    public final Object provide(Object component, String... args) throws Throwable {
        if (isSingleton()) {
            Singletons singletons = ComponentManager.singletonsMap.get(componentType);
            if (singletons == null)
                return doProvide(component, args);
            Object value = singletons.getSingleton(this);
            if (value != null)
                return value;
            synchronized (this) {
                Object v = singletons.getSingleton(this);
                if (v != null)
                    return v;
                v = doProvide(component, args);
                if (v != null) {
                    singletons.addSingleton(this, v);
                }
                return v;
            }
        } else {
            return doProvide(component, args);
        }
    }

    @Override
    public Object provideDirect(Object component, Object... args) throws Throwable {
        return invoke(component, args);
    }

    public Object doProvide(Object component, String... args) throws Throwable {
        return invoke(component);
    }

    protected Object invoke(Object component, Object... args) throws Throwable {
        if (needIPC()) {
            LazyInjectIPC ipcClient = InjectIPCClientManager.getClient(componentType);
            if (ipcClient == null)
                return null;
            return ipcClient.remoteProvide(componentType, key(), args);
        } else {
            if (args == null) {
                return providerMethod.invoke(component);
            } else {
                return providerMethod.invoke(component, args);
            }
        }
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean needIPC() {
        return needIPC;
    }

    @Override
    public void setIPC(boolean ipc) {
        this.needIPC = ipc;
    }

    @Override
    public String key() {
        return providerMethod.toGenericString();
    }

}
