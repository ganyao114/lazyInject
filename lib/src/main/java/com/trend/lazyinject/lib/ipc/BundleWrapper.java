package com.trend.lazyinject.lib.ipc;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

import com.trend.lazyinject.lib.log.LOG;
import com.trend.lazyinject.lib.utils.BundlerCompat;
import com.trend.lazyinject.lib.utils.ValidateUtil;

import java.io.Serializable;

public class BundleWrapper {

    public final static int TYPE_NULL = 0;
    public final static int TYPE_SERIAL = 1;
    public final static int TYPE_PARCEL = 2;
    public final static int TYPE_BINDER = 3;

    public final static String KEY_ARGS = "args_types";

    public final static String KEY_RET_TYPE = "ret_type";
    public final static String KEY_RET_VALUE = "ret_value";

    public static Bundle wrapArgs(Bundle bundle, Object[] args) {

        if (ValidateUtil.isEmpty(args))
            return bundle;

        int[] argTypes = new int[args.length];

        for (int i = 0;i < args.length;i ++) {
            Object arg = args[i];
            if (arg == null) {
                argTypes[i] = TYPE_NULL;
            } else if (arg instanceof Serializable) {
                argTypes[i] = TYPE_SERIAL;
                bundle.putSerializable(getParKey(i), (Serializable) arg);
            } else if (arg instanceof Parcelable) {
                argTypes[i] = TYPE_PARCEL;
                bundle.putParcelable(getParKey(i), (Parcelable) arg);
            } else if (arg instanceof IBinder) {
                argTypes[i] = TYPE_BINDER;
                BundlerCompat.putBinder(bundle, getParKey(i), (IBinder) arg);
            } else {
                LOG.LOGE("BundleWrapper", "find illegal arg type!");
                argTypes[i] = TYPE_NULL;
            }
        }

        bundle.putIntArray(KEY_ARGS, argTypes);

        return bundle;

    }

    public static Object[] unWrapArgs(Bundle bundle) {

        if (bundle == null)
            return null;

        int[] argTypes = bundle.getIntArray(KEY_ARGS);

        if (ValidateUtil.isEmpty(argTypes))
            return null;

        Object[] args = new Object[argTypes.length];

        for (int i = 0;i < args.length;i ++) {
            switch (argTypes[i]) {
                case TYPE_SERIAL:
                    args[i] = bundle.getSerializable(getParKey(i));
                    break;
                case TYPE_PARCEL:
                    args[i] = bundle.getParcelable(getParKey(i));
                    break;
                case TYPE_BINDER:
                    args[i] = BundlerCompat.getBinder(bundle, getParKey(i));
                    break;
            }
        }

        return args;
    }

    public static Bundle wrapRet(Object object) {
        if (object == null)
            return null;
        Bundle bundle = new Bundle();

        if (object instanceof Serializable) {
            bundle.putInt(KEY_RET_TYPE, TYPE_SERIAL);
            bundle.putSerializable(KEY_RET_VALUE, (Serializable) object);
        } else if (object instanceof Parcelable) {
            bundle.putInt(KEY_RET_TYPE, TYPE_PARCEL);
            bundle.putParcelable(KEY_RET_VALUE, (Parcelable) object);
        } else if (object instanceof IBinder) {
            bundle.putInt(KEY_RET_TYPE, TYPE_BINDER);
            BundlerCompat.putBinder(bundle, KEY_RET_VALUE, (IBinder) object);
        } else {
            LOG.LOGE("BundleWrapper", "find illegal return type!");
            return null;
        }

        return bundle;
    }

    public static Object unWrapRet(Bundle bundle) {

        if (bundle == null)
            return null;

        int retType = bundle.getInt(KEY_RET_TYPE, TYPE_NULL);

        if (retType == TYPE_NULL)
            return null;

        Object retValue = null;

        switch (retType) {
            case TYPE_SERIAL:
                retValue = bundle.getSerializable(KEY_RET_VALUE);
                break;
            case TYPE_PARCEL:
                retValue = bundle.getParcelable(KEY_RET_VALUE);
                break;
            case TYPE_BINDER:
                retValue = BundlerCompat.getBinder(bundle, KEY_RET_VALUE);
                break;
        }

        return retValue;
    }

    private static String getParKey(int i) {
        return "arg_" + i;
    }
}
