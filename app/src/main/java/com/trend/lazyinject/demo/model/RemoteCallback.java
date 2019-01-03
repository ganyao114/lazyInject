package com.trend.lazyinject.demo.model;

import android.os.RemoteException;
import android.util.Log;

public class RemoteCallback extends BinderCallback.Stub {
    @Override
    public void callback(int process, boolean done) throws RemoteException {
        Log.d("RemoteCallback", "progress: " + process + " done:" + done);
    }
}
