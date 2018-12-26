package com.trend.lazyinject.demo.component;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;

import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.Provide;
import com.trend.lazyinject.demo.model.BaseModel;
import com.trend.lazyinject.demo.model.BinderCallback;
import com.trend.lazyinject.demo.model.ModelA;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by swift_gan on 2018/4/17.
 */
@Component
public interface TestComponent<T extends Serializable & List> extends Serializable {
    @Provide
    List<String> provide1();
    @Provide
    ArrayList<Integer> provide2();
    @Provide
    ArrayList<? extends BaseModel> provide3();
    @Provide(singleton = true)
    ModelA provide4(Map<String,BaseModel> strings,String test);
    @Provide
    Map<String,BaseModel> provide5();
    @Provide
    Map<String, ? extends ModelA> provide6();
    @Provide
    Map<String,T> provide7();
    @Provide
    Bundle provide8TestForParcel();
    Bundle invokeTestForParcel(String a, ModelA modelA, Bundle bundle, IBinder callback);
    @Provide
    Application c();
}
