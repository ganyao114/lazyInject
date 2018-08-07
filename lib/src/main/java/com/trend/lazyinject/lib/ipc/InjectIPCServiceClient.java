package com.trend.lazyinject.lib.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

import com.trend.lazyinject.lib.LazyInject;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InjectIPCServiceClient implements LazyInjectIPC {

    Class<? extends IPCService> serviceClass;

    Context context = LazyInject.context();

    volatile IBinder binder;

    public InjectIPCServiceClient(Class<? extends IPCService> serviceClass) {
        this.serviceClass = serviceClass;
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkAndBindService();
            }
        }).start();
    }

    @Override
    public Serializable remoteProvide(Class componentType, String providerKey, Object[] args) {
        IBinder binder = checkAndBindService();
        if (binder == null)
            return null;
        Parcel pars = Parcel.obtain();
        Parcel ret = Parcel.obtain();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CTYPE, componentType);
        bundle.putString(KEY_PKEY, providerKey);
        bundle.putSerializable(KEY_ARGS, args);
        pars.writeBundle(bundle);
        try {
            binder.transact(OP_PROVIDE, pars, ret, 0);
            Serializable res = ret.readSerializable();
            return res;
        } catch (RemoteException e) {
        } finally {
            pars.recycle();
            ret.recycle();
        }
        return null;
    }

    @Override
    public Serializable remoteInvoke(Class componentType, String providerKey, Object[] args) {
        IBinder binder = checkAndBindService();
        if (binder == null)
            return null;
        Parcel pars = Parcel.obtain();
        Parcel ret = Parcel.obtain();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CTYPE, componentType);
        bundle.putString(KEY_PKEY, providerKey);
        bundle.putSerializable(KEY_ARGS, args);
        pars.writeBundle(bundle);
        try {
            binder.transact(OP_INVOKE, pars, ret, 0);
            Serializable res = ret.readSerializable();
            return res;
        } catch (RemoteException e) {
        } finally {
            pars.recycle();
            ret.recycle();
        }
        return null;
    }

    private IBinder checkAndBindService() {
        if (binder != null && binder.isBinderAlive())
            return binder;
        if (Looper.myLooper() == Looper.getMainLooper())
            return null;
        synchronized (this) {
            if (binder != null && binder.isBinderAlive())
                return binder;
            final BlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>(1);
            context.bindService(new Intent(context, serviceClass), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = service;
                    try {
                        q.put(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);
            try {
                q.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (binder != null && binder.isBinderAlive()) {
                return binder;
            } else {
                return null;
            }
        }
    }

}
