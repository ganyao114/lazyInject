package com.trend.lazyinject.demo.model;

/**
 * Created by swift_gan on 2018/4/17.
 */

public class ModelA extends BaseModel {
    String str = "def";

    public ModelA(String str) {
        this.str = str;
    }

    public ModelA() {
    }

    @Override
    public String toString() {
        return "ModelA{" +
                "str='" + str + '\'' +
                '}';
    }
}
