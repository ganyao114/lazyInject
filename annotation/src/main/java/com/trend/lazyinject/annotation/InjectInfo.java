package com.trend.lazyinject.annotation;

public class InjectInfo {

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

}
