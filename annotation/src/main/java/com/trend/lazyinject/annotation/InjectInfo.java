package com.trend.lazyinject.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class InjectInfo implements Inject {

    public Class component;
    public boolean alwaysRefresh;
    public boolean nullProtect;
    public String[] args;

    public InjectInfo(Class component, boolean alwaysRefresh, boolean nullProtect, String[] args) {
        this.component = component;
        this.alwaysRefresh = alwaysRefresh;
        this.nullProtect = nullProtect;
        this.args = args;
    }

    @Override
    public String toString() {
        return "InjectInfo{" +
                "component=" + component +
                ", alwaysRefresh=" + alwaysRefresh +
                ", nullProtect=" + nullProtect +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Inject.class;
    }

    @Override
    public Class<?> component() {
        return component;
    }

    @Override
    public boolean alwaysRefresh() {
        return alwaysRefresh;
    }

    @Override
    public String[] args() {
        return args;
    }

    @Override
    public boolean nullProtect() {
        return nullProtect;
    }
}
