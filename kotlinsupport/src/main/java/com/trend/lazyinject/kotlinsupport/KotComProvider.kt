package com.trend.lazyinject.kotlinsupport

import com.trend.lazyinject.lib.component.ComponentManager
import com.trend.lazyinject.lib.di.DIImpl
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaGetter

/**
 * Created by ganyao on 2017/12/5.
 */
public fun <T> injectComponent() : ReadOnlyProperty<Any, T> = ComponentProvider<Any,T>();

public fun <T> inject(component : Any = None::class, alwaysRefresh: Boolean = false, vararg args: Any) : ReadOnlyProperty<Any, T> = ElementProvider<Any,T>(component, alwaysRefresh, args);

public fun <T> inject(component : Any = None::class, alwaysRefresh: Boolean = false) : ReadOnlyProperty<Any, T> = ElementProviderNoArgs<Any,T>(component, alwaysRefresh);

private class ComponentProvider<R,T> : ReadOnlyProperty<R,T> {

    private object EMPTY
    private var value: Any? = EMPTY

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == EMPTY) {
            value = ComponentManager.getComponent(property.javaGetter?.returnType);
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

}

private class ElementProvider<R,T>(component: Any = None::class, alwaysRefresh: Boolean = false, vararg args: Any) : ReadOnlyProperty<R,T> {

    private object EMPTY
    private var value: Any? = EMPTY
    private var component : Any = component;
    private var args : Any = args;
    private var alwaysRefresh: Boolean = alwaysRefresh;

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == null || value == EMPTY || alwaysRefresh) {
            if (component == None::class) {
                component = property.javaGetter?.returnType?.enclosingClass?:None::class;
            }
            if (component == None::class) {
                @Suppress("UNCHECKED_CAST")
                return value as T;
            }
            if (component is Class<*>) {
                value = DIImpl.providerValue(component as Class<*>, property.javaGetter?.genericReturnType, args);
            } else if (component is KClass<*>) {
                value = DIImpl.providerValue((component as KClass<*>).java, property.javaGetter?.genericReturnType, args);
            }
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

}

private class ElementProviderNoArgs<R,T>(component: Any = None::class, alwaysRefresh: Boolean = false) : ReadOnlyProperty<R,T> {

    private object EMPTY
    private var value: Any? = EMPTY
    private var component : Any = component;
    private var alwaysRefresh: Boolean = alwaysRefresh;

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == null || value == EMPTY || alwaysRefresh) {
            if (component == None::class) {
                component = property.javaGetter?.returnType?.enclosingClass?:None::class;
            }
            if (component == None::class) {
                @Suppress("UNCHECKED_CAST")
                return value as T;
            }
            if (component is Class<*>) {
                value = DIImpl.providerValue(component as Class<*>, property.javaGetter?.genericReturnType, null);
            } else if (component is KClass<*>) {
                value = DIImpl.providerValue((component as KClass<*>).java, property.javaGetter?.genericReturnType, null);
            }
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

}

class None{}