package com.trend.lazyinject.aopweave.config;

public class WeaveConfig {

    public boolean enable = true
    public String[] includes

    public void enable(boolean enable) {
        this.enable = enable
    }

    public void includes(String[] includes) {
        this.includes = includes
    }
}