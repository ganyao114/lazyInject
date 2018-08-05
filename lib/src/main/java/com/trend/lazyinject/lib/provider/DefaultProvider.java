package com.trend.lazyinject.lib.provider;

import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.component.Singletons;

import java.lang.reflect.Method;

public class DefaultProvider implements IProvider {

    protected Method providerMethod;
    private boolean singleton;

    public DefaultProvider(Method providerMethod) {
        this.providerMethod = providerMethod;
    }

    @Override
    public final Object provide(Object component, String... args) throws Throwable {
        if (isSingleton()) {
            Singletons singletons = ComponentManager.singletonsMap.get(component);
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

    public Object doProvide(Object component, String... args) throws Throwable {
        return providerMethod.invoke(component);
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
    public String key() {
        return providerMethod.toGenericString();
    }

}
