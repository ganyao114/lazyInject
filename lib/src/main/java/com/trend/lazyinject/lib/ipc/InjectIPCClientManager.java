package com.trend.lazyinject.lib.ipc;

import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InjectIPCClientManager {

    static Map<String,LazyInjectIPC> ipcClients = new ConcurrentHashMap<>();

    static Map<Class, String> componentToProcesses = new ConcurrentHashMap<>();

    public static LazyInjectIPC getClient(String process) {
        if (TextUtils.isEmpty(process))
            return null;
        LazyInjectIPC ipc = ipcClients.get(process);
        if (ipc != null)
            return ipc;
        synchronized (process.intern()) {
            ipc = ipcClients.get(process);
            if (ipc != null)
                return ipc;
            ipc = new InjectIPCProviderClient(process);
            ipcClients.put(process, ipc);
            return ipc;
        }
    }

    public static LazyInjectIPC getClient(Class component) {
        return getClient(getIPCProcess(component));
    }

    public static void setIPCProcess(Class component, String process) {
        componentToProcesses.put(component, process);
    }

    public static String getIPCProcess(Class component) {
        return componentToProcesses.get(component);
    }

}
