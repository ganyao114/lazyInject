package com.trend.lazyinject.lib.provider;

import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.lang.reflect.Method;

public class StringArgsProvider extends DefaultProvider {

    int length;

    public StringArgsProvider(Class componentType, Method providerMethod, int length) {
        super(componentType, providerMethod);
        this.length = length;
    }

    @Override
    public Object doProvide(Object component, String... args) throws Throwable {
        if (!ValidateUtil.isEmpty(args)) {
            return invoke(component, initStrings(args));
        } else {
            return invoke(component, initStrings(new String[length]));
        }
    }

    protected Object[] initStrings(String... strings) {
        if (strings == null)
            return strings;
        for (int i = 0;i < strings.length;i ++) {
            if (strings[i] == null) {
                strings[i] = "";
            }
        }
        return strings;
    }

}
