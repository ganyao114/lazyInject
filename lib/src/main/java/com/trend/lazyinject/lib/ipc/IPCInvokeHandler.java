package com.trend.lazyinject.lib.ipc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IPCInvokeHandler implements InvocationHandler {

    Class componentType;
    LazyInjectIPC ipcClient;

    public IPCInvokeHandler(Class componentType) {
        this.componentType = componentType;
        ipcClient = InjectIPCClientManager.getClient(componentType);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        if (ipcClient != null) {
            return ipcClient.remoteInvoke(componentType, method.toGenericString(), args);
        } else {
            return null;
        }
    }
}
