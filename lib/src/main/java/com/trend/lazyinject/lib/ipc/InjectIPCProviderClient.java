package com.trend.lazyinject.lib.ipc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.trend.lazyinject.lib.LazyInject;
import com.trend.lazyinject.lib.utils.ContentProviderCompat;

import java.io.Serializable;

public class InjectIPCProviderClient implements LazyInjectIPC {

    Context context = LazyInject.context();

    String authority;
    Uri uri;

    public InjectIPCProviderClient(String authority) {
        this.authority = authority;
        uri = Uri.parse("content://" + authority);
    }

    @Override
    public Object remoteProvide(Class componentType, String providerKey, Object[] args) {

        Bundle bundle = new Bundle();

        bundle.putSerializable(KEY_CTYPE, componentType);
        bundle.putString(KEY_PKEY, providerKey);
        BundleWrapper.wrapArgs(bundle, args);

        Bundle res = ContentProviderCompat.call(context, uri, OP_PROVIDE_S, null, bundle);

        if (res == null)
            return null;

        res.setClassLoader(getClass().getClassLoader());

        return BundleWrapper.unWrapRet(res);
    }

    @Override
    public Object remoteInvoke(Class componentType, String providerKey, Object[] args) {

        Bundle bundle = new Bundle();

        bundle.putSerializable(KEY_CTYPE, componentType);
        bundle.putString(KEY_PKEY, providerKey);
        BundleWrapper.wrapArgs(bundle, args);

        Bundle res = ContentProviderCompat.call(context, uri, OP_INVOKE_S, null, bundle);

        if (res == null)
            return null;

        res.setClassLoader(getClass().getClassLoader());

        return BundleWrapper.unWrapRet(res);
    }


}
