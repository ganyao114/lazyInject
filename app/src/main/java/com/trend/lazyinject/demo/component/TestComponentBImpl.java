package com.trend.lazyinject.demo.component;

import android.content.Context;

import com.trend.lazyinject.annotation.ComponentImpl;
import com.trend.lazyinject.demo.application.DemoApplication;

@ComponentImpl
public class TestComponentBImpl implements TestComponentB {
    @Override
    public Context c() {
        return DemoApplication.application;
    }

    @Override
    public Inner inner() {
        return new Inner() {
            @Override
            public void doSth() {

            }
        };
    }
}
