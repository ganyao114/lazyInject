package com.trend.lazyinject.lib.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.Serializable;

public class IPCService extends Service {

    LazyInjectIPC service;
    Binder binder;

    @Override
    public void onCreate() {
        service = new InjectIPCService();
        binder = new IPCBinder();
    }

    public class IPCBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

            Bundle bundle = data.readBundle(getClass().getClassLoader());
            if (bundle == null)
                return true;

            Class componentType = (Class) bundle.getSerializable(LazyInjectIPC.KEY_CTYPE);
            String providerKey = bundle.getString(LazyInjectIPC.KEY_PKEY);
            Serializable[] args = (Serializable[]) bundle.getSerializable(LazyInjectIPC.KEY_ARGS);

            if (componentType == null || providerKey == null)
                return true;

            Object ret = null;

            switch (code) {
                case LazyInjectIPC.OP_PROVIDE:
                    ret = service.remoteProvide(componentType, providerKey, args);
                    break;
                case LazyInjectIPC.OP_INVOKE:
                    ret = service.remoteInvoke(componentType, providerKey, args);
                    break;
            }

            if (flags == 0 && ret != null) {
                reply.writeSerializable((Serializable) ret);
            }

            return true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
