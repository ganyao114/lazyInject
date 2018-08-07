package com.trend.lazyinject.lib.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.io.Serializable;

public class InjectIPCProvider extends ContentProvider {

    volatile LazyInjectIPC service;

    @Override
    public Bundle call(String method, String arg, Bundle bundle) {

        if (bundle == null)
            return null;

        bundle.setClassLoader(getClass().getClassLoader());
        Class componentType = (Class) bundle.getSerializable(LazyInjectIPC.KEY_CTYPE);
        String providerKey = bundle.getString(LazyInjectIPC.KEY_PKEY);
        Object[] args = BundleWrapper.unWrapArgs(bundle);

        if (componentType == null || providerKey == null)
            return null;

        Object ret = null;

        switch (method) {
            case LazyInjectIPC.OP_INVOKE_S:
                ret = service.remoteInvoke(componentType, providerKey, args);
                break;
            case LazyInjectIPC.OP_PROVIDE_S:
                ret = service.remoteProvide(componentType, providerKey, args);
                break;
        }

        if (ret != null) {
            Bundle resBundle = BundleWrapper.wrapRet(ret);
            return resBundle;
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        service = new InjectIPCService();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }



}
