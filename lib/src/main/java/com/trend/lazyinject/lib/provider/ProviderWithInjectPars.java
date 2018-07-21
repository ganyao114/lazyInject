package com.trend.lazyinject.lib.provider;

import com.trend.lazyinject.annotation.Inject;
import com.trend.lazyinject.annotation.InjectComponent;
import com.trend.lazyinject.lib.component.ComponentManager;
import com.trend.lazyinject.lib.di.DIImpl;
import com.trend.lazyinject.lib.proxy.InterfaceProxy;
import com.trend.lazyinject.lib.utils.ReflectUtils;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ProviderWithInjectPars extends DefaultProvider {

    ProviderInfo[] providerInfos;

    public ProviderWithInjectPars(Method providerMethod, ProviderInfo[] providerInfos) {
        super(providerMethod);
        this.providerInfos = providerInfos;
    }

    @Override
    public Object doProvide(Object component, String... args) throws Throwable {
        Object[] pars = new Object[providerInfos.length];
        int curString = 0;
        for (int i = 0;i < pars.length;i ++) {
            ProviderInfo providerInfo = providerInfos[i];
            if (providerInfo == null) {

            } else if (providerInfo.providerType == ProviderType.String) {
                if (!ValidateUtil.isEmpty(args) && curString < args.length) {
                    pars[i] = args[curString];
                    curString++;
                }
            } else if (providerInfo.providerType == ProviderType.Inject){
                Class<?> componentType = providerInfo.inject.component();
                if (componentType == Inject.None.class) {
                    componentType = ReflectUtils.getRawType(providerInfo.type).getEnclosingClass();
                    if (componentType == null || componentType == Object.class)
                        continue;
                }
                Object value = DIImpl.providerValue(componentType, providerInfo.type, providerInfo.inject.args());
                if (value == null && providerInfo.inject.nullProtect()) {
                    value = InterfaceProxy.make(ReflectUtils.getRawType(providerInfo.type));
                }
                pars[i] = value;
            } else {
                Object value = ComponentManager.getComponent(ReflectUtils.getRawType(providerInfo.type));
                if (value == null && providerInfo.injectComponent.nullProtect()) {
                    value = InterfaceProxy.make(ReflectUtils.getRawType(providerInfo.type));
                }
                pars[i] = value;
            }
        }
        return providerMethod.invoke(component, pars);
    }

    public static class ProviderInfo {

        public ProviderType providerType = ProviderType.Inject;

        public Type type;
        public Inject inject;
        public InjectComponent injectComponent;

        public ProviderInfo() {
            providerType = ProviderType.String;
        }

        public ProviderInfo(Type componentType, InjectComponent injectComponent) {
            providerType = ProviderType.InjectComponent;
            this.type = componentType;
            this.injectComponent = injectComponent;
        }

        public ProviderInfo(Type type, Inject inject) {
            providerType = ProviderType.Inject;
            this.type = type;
            this.inject = inject;
        }
    }

    public enum ProviderType {
        String,
        Inject,
        InjectComponent
    }

    public static class FakeInject implements Inject {

        Class<?> component;

        public FakeInject(Class<?> component) {
            this.component = component;
        }

        @Override
        public Class<?> component() {
            return component;
        }

        @Override
        public boolean alwaysRefresh() {
            return false;
        }

        @Override
        public String[] args() {
            return new String[0];
        }

        @Override
        public boolean nullProtect() {
            return false;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Inject.class;
        }
    }

}
