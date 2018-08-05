package com.trend.lazyinject.lib.ipc;

import java.io.Serializable;

public class InjectIPCClient implements LazyInjectIPC {
    @Override
    public Serializable remoteProvide(Class componenetType, String providerKey, Serializable[] args) {
        return null;
    }

    @Override
    public Serializable remoteInvoke(Class componenetType, String providerKey, Serializable[] args) {
        return null;
    }
}
