package com.trend.lazyinject.aopweave.config;

public class WeaveConfig {

    public boolean enable = true
    public boolean optimize = false
    public String[] includes

    public void optimize(boolean optimize) {
        this.optimize = optimize
    }

    public void enable(boolean enable) {
        this.enable = enable
    }

    public void includes(String[] includes) {
        this.includes = includes
    }
}