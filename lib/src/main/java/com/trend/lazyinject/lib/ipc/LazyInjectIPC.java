// LazyInjectIPC.aidl
package com.trend.lazyinject.lib.ipc;

// Declare any non-default types here with import statements

import java.io.Serializable;

public interface LazyInjectIPC {

    int OP_PROVIDE = 1;
    int OP_INVOKE = 2;

    String KEY_CTYPE = "CTYPE";
    String KEY_PKEY = "PKEY";
    String KEY_ARGS = "ARGS";

    Serializable remoteProvide(Class componenetType, String providerKey, Serializable[] args);
    Serializable remoteInvoke(Class componenetType, String providerKey, Serializable[] args);

}
