package com.trend.lazyinject.demo.application;

import android.app.Application;

import com.trend.lazyinject.annotation.FieldGetHook;
import com.trend.lazyinject.buildmap.Auto_ComponentBuildMap;
import com.trend.lazyinject.lib.LazyInject;

/**
 * Created by swift_gan on 2018/4/17.
 */

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LazyInject.init(this);
        LazyInject.addBuildMap(Auto_ComponentBuildMap.class);
    }
}
