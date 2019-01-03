package com.trend.lazyinject.aopweave.infos;

import javassist.CtClass

public class InjectAnnoInfo {

    public CtClass component
    public boolean alwaysRefresh
    public boolean nullProtect
    public String args

    InjectAnnoInfo() {
    }

    public InjectAnnoInfo(CtClass component, boolean alwaysRefresh, boolean nullProtect, String args) {
        this.component = component
        this.alwaysRefresh = alwaysRefresh
        this.nullProtect = nullProtect
        this.args = args
    }

}
