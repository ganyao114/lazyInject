package com.trend.lazyinject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface ComponentImpl {
    String component() default "";
    String name() default "default";
    boolean cache() default true;
    String process() default "";
}
