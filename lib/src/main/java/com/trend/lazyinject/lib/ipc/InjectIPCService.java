package com.trend.lazyinject.lib.ipc;

import android.util.Log;

import java.io.Serializable;

public class InjectIPCService implements LazyInjectIPC {
    @Override
    public Serializable remoteProvide(Class componenetType, String providerKey, Serializable[] args) {
        Log.e("gy", "type: " + componenetType.getName() + " method: " + providerKey + " args: " + args.toString());
        return "nihao";
    }

    @Override
    public Serializable remoteInvoke(Class componenetType, String providerKey, Serializable[] args) {
        Log.e("gy", "type: " + componenetType.getName() + " method: " + providerKey + " args: " + args.toString());
        return "nihao";
    }
}
