package com.trend.lazyinject.lib.provider;

public interface IProvider {
    Object provide(Object component, String... args) throws Throwable;
    Object provideDirect(Object component, Object... args) throws Throwable;
    boolean isSingleton();
    void setSingleton(boolean singleton);
    boolean needIPC();
    void setIPC(boolean ipc);
    String key();
}
