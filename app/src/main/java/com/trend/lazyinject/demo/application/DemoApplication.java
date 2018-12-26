package com.trend.lazyinject.demo.application;

import android.app.Application;

import com.trend.lazyinject.buildmap.Auto_ComponentBuildMap;
import com.trend.lazyinject.lib.LazyInject;

/**
 * Created by swift_gan on 2018/4/17.
 */

public class DemoApplication extends Application {

    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        LazyInject.init(this);
        LazyInject.addBuildMap(Auto_ComponentBuildMap.class);
    }
}
