package com.trend.lazyinject.lib.ipc;

import com.trend.lazyinject.lib.di.DIImpl;

import java.io.Serializable;

public class InjectIPCService implements LazyInjectIPC {
    @Override
    public Object remoteProvide(Class componentType, String providerKey, Object[] args) {
        return DIImpl.providerValue(componentType, providerKey, args);
    }

    @Override
    public Object remoteInvoke(Class componentType, String providerKey, Object[] args) {
        return DIImpl.invokeDirect(componentType, providerKey, args);
    }
}
