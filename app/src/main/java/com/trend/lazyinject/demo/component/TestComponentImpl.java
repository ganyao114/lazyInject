package com.trend.lazyinject.demo.component;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.trend.lazyinject.annotation.ComponentImpl;
import com.trend.lazyinject.annotation.DebugLog;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.BinderCallback;
import com.trend.lazyinject.demo.model.ModelA;
import com.trend.lazyinject.demo.model.RemoteCallback;
import com.trend.lazyinject.lib.LazyInject;
import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.utils.ProcessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by swift_gan on 2018/4/17.
 */
@ComponentImpl(process = "com.trend.lazyinject.demo.p1")
public class TestComponentImpl implements TestComponent {

    public TestComponentImpl() {
    }


    @DebugLog
    @Override
    public List<String> provide1() {
        return new ArrayList<>();
    }
    @DebugLog
    @Override
    public ArrayList<Integer> provide2() {
        return new ArrayList<>();
    }
    @DebugLog
    @Override
    public ArrayList<? extends BaseModel> provide3() {
        return new ArrayList<>();
    }
    @DebugLog
    @Override
    public ModelA provide4(Map strings,String test) {
        return new ModelA(ProcessUtils.getProcessName(LazyInject.context()));
    }
    @DebugLog
    @Override
    public Map<String, BaseModel> provide5() {
        return new HashMap<>();
    }
    @DebugLog
    @Override
    public Map<String, ? extends ModelA> provide6() {
        return new HashMap<>();
    }
    @DebugLog
    @Override
    public Map<String, ? extends ArrayList> provide7() {
        return new HashMap<>();
    }

    @Override
    public Bundle provide8TestForParcel() {
        return new Bundle();
    }

    @Override
    public Bundle invokeTestForParcel(String a, ModelA modelA, Bundle bundle, IBinder callback) {
        Log.e("ipc invoke - " + a + modelA.toString(), bundle.toString());
        BinderCallback cb = BinderCallback.Stub.asInterface(callback);
        new Thread(() -> {
            int i = 10;
            while (i > 0) {
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cb.asBinder().isBinderAlive()) {
                    try {
                        cb.callback(i, i == 1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                i--;
            }
        }).start();
        return new Bundle();
    }
}
