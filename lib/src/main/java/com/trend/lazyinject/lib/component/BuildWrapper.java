package com.trend.lazyinject.lib.component;

/**
 * Created by swift_gan on 2017/12/28.
 */

public class BuildWrapper<T> {

    public T component;
    public boolean noCache;

    public BuildWrapper(T component, boolean noCache) {
        this.component = component;
        this.noCache = noCache;
    }

    @Override
    public String toString() {
        return "BuildWrapper{" +
                "component=" + component +
                ", noCache=" + noCache +
                '}';
    }
}
