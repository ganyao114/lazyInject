package com.trend.lazyinject.lib.utils;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gy939 on 2017/1/15.
 */

public class ValidateUtil {

    public static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string);
    }

    public static boolean isEmpty(List list) {
        if (list == null)
            return true;
        if (list.size() == 0)
            return true;
        return false;
    }

    public static boolean isEmpty(Set list) {
        if (list == null)
            return true;
        if (list.size() == 0)
            return true;
        return false;
    }

    public static boolean isEmpty(Object[] list) {
        if (list == null)
            return true;
        if (list.length == 0)
            return true;
        return false;
    }

    public static boolean isEmpty(Map map) {
        if (map == null)
            return true;
        if (map.size() == 0)
            return true;
        return false;
    }

}
