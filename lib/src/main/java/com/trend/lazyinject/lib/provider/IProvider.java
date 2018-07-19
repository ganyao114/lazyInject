package com.trend.lazyinject.lib.provider;

public interface IProvider {
    Object provide(Object component, String... args) throws Throwable;
}
