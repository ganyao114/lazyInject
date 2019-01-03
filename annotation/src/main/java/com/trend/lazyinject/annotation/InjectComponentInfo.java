package com.trend.lazyinject.annotation;

import java.lang.annotation.Annotation;

public class InjectComponentInfo implements InjectComponent {

    public String value;
    public boolean alwaysRefresh;
    public boolean nullProtect;

    public InjectComponentInfo(String value, boolean alwaysRefresh, boolean nullProtect) {
        this.value = value;
        this.alwaysRefresh = alwaysRefresh;
        this.nullProtect = nullProtect;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean alwaysRefresh() {
        return alwaysRefresh;
    }

    @Override
    public boolean nullProtect() {
        return nullProtect;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return InjectComponent.class;
    }

    @Override
    public String toString() {
        return "InjectComponentInfo{" +
                "value='" + value + '\'' +
                ", alwaysRefresh=" + alwaysRefresh +
                ", nullProtect=" + nullProtect +
                '}';
    }
}
