package com.trend.lazyinject.lib.ipc;

import com.trend.lazyinject.lib.di.DIImpl;

import java.io.Serializable;

public class InjectIPCService implements LazyInjectIPC {
    @Override
    public Serializable remoteProvide(Class componentType, String providerKey, Serializable[] args) {
        return (Serializable) DIImpl.providerValue(componentType, providerKey, args);
    }

    @Override
    public Serializable remoteInvoke(Class componentType, String providerKey, Serializable[] args) {
        return (Serializable) DIImpl.invokeDirect(componentType, providerKey, args);
    }
}
