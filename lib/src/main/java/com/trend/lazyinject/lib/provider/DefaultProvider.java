package com.trend.lazyinject.lib.provider;

import java.lang.reflect.Method;

public class DefaultProvider implements IProvider {

    protected Method providerMethod;

    public DefaultProvider(Method providerMethod) {
        this.providerMethod = providerMethod;
    }

    @Override
    public Object provide(Object component, String... args) throws Throwable {
        return providerMethod.invoke(component);
    }

}
