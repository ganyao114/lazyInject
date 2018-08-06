package com.trend.lazyinject.lib.ipc;

import com.trend.lazyinject.lib.di.DIImpl;

import java.io.Serializable;

public class InjectIPCService implements LazyInjectIPC {
    @Override
    public Serializable remoteProvide(Class componenetType, String providerKey, Serializable[] args) {
        return (Serializable) DIImpl.providerValue(componenetType, providerKey, args);
    }

    @Override
    public Serializable remoteInvoke(Class componenetType, String providerKey, Serializable[] args) {
        return (Serializable) DIImpl.invokeDirect(componenetType, providerKey, args);
    }
}
