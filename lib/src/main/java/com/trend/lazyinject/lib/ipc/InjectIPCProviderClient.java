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
    public Serializable remoteProvide(Class componenetType, String providerKey, Serializable[] args) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CTYPE, componenetType);
        bundle.putString(KEY_PKEY, providerKey);
        bundle.putSerializable(KEY_ARGS, args);

        Bundle res = ContentProviderCompat.call(context, uri, OP_PROVIDE_S, null, bundle);

        if (res == null)
            return null;

        res.setClassLoader(getClass().getClassLoader());

        return res.getSerializable(KEY_RET);
    }

    @Override
    public Serializable remoteInvoke(Class componenetType, String providerKey, Serializable[] args) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CTYPE, componenetType);
        bundle.putString(KEY_PKEY, providerKey);
        bundle.putSerializable(KEY_ARGS, args);

        Bundle res = ContentProviderCompat.call(context, uri, OP_INVOKE_S, null, bundle);

        if (res == null)
            return null;

        res.setClassLoader(getClass().getClassLoader());

        return res.getSerializable(KEY_RET);
    }


}
