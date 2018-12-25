package com.trend.lazyinject.demo.model;

import com.trend.lazyinject.annotation.InjectTest;
import com.trend.lazyinject.demo.component.TestComponent;

import java.io.Serializable;

/**
 * Created by swift_gan on 2018/4/17.
 */

public class BaseModel implements Serializable {

    @InjectTest
    public ModelA modelA;

}
