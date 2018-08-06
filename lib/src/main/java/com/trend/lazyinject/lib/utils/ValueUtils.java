package com.trend.lazyinject.lib.utils;

import java.io.Serializable;

public class ValueUtils {
    public static Serializable[] ObjectToSerial(Object... objects) {
        if (ValidateUtil.isEmpty(objects))
            return null;
        Serializable[] serializable = new Serializable[objects.length];
        for (int i = 0;i < objects.length;i ++) {
            serializable[i] = (Serializable) objects[i];
        }
        return serializable;
    }
}
