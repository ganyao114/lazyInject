package com.trend.lazyinject.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by swift_gan on 2017/12/22.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Exclude {
}
