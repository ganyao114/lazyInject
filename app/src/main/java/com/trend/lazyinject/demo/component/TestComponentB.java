package com.trend.lazyinject.demo.component;

import android.content.Context;

import com.trend.lazyinject.annotation.Component;
import com.trend.lazyinject.annotation.Provide;

@Component
public interface TestComponentB {
    @Provide
    Context c();
}
